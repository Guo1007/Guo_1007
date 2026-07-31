<template>
  <div class="ai-chat-page">
    <!-- Breadcrumb -->
    <div class="page-breadcrumb">
      <button class="breadcrumb-back" @click="goBack" title="返回">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <router-link to="/">首页</router-link>
      <span>/</span>
      <span class="current">智能客服</span>
    </div>

    <div class="chat-container">
      <!-- 左侧聊天区 -->
      <section class="chat-main">
        <div class="chat-header">
          <div class="header-left">
            <div class="bot-avatar">智</div>
            <div class="header-text">
              <h2>小智 AI 助手</h2>
              <p>随时为您解答家具选购疑问</p>
            </div>
          </div>
          <div class="header-actions">
            <button class="icon-btn" @click="newChat" title="新对话">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
            </button>
          </div>
        </div>

        <!-- 消息区 -->
        <div class="chat-body" ref="bodyRef">
          <div v-if="messages.length === 0" class="welcome-block">
            <div class="welcome-emoji">🛋️</div>
            <h3>你好，我是小智</h3>
            <p>家具商城的智能客服助手，试试问我：</p>
            <div class="quick-chips">
              <button
                v-for="q in quickQuestions"
                :key="q"
                class="chip"
                @click="sendMessage(q)"
              >{{ q }}</button>
            </div>
          </div>

          <div
            v-for="(msg, i) in messages"
            :key="i"
            class="msg-row"
            :class="msg.role"
          >
            <div class="msg-avatar">
              <span v-if="msg.role === 'assistant'" class="avatar-text">小智</span>
              <span v-else>👤</span>
            </div>
            <div class="msg-body">
              <div class="msg-bubble" v-html="fmt(msg.content, cacheVersion)"></div>
              <span v-if="msg.role === 'assistant' && i === messages.length - 1 && loading" class="typing-dots">
                <i></i><i></i><i></i>
              </span>
            </div>
          </div>

          <!-- 等待首条AI回复时的加载动画 -->
          <div v-if="loading && messages.length > 0 && messages[messages.length - 1].role === 'user'" class="msg-row assistant">
            <div class="msg-avatar"><span class="avatar-text">小智</span></div>
            <div class="msg-body">
              <div class="msg-bubble thinking">
                <div class="typing-dots"><i></i><i></i><i></i></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="chat-footer">
          <div class="input-row">
            <input
              v-model="inputMessage"
              placeholder="输入您的问题..."
              :disabled="loading"
              @keydown.enter="sendMessage()"
              ref="inputRef"
            />
            <button
              class="send-btn"
              :disabled="!inputMessage.trim() || loading"
              @click="sendMessage()"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
            </button>
          </div>
        </div>
      </section>

      <!-- 右侧侧边栏 -->
      <aside class="chat-sidebar">
        <div class="sidebar-card">
          <h4>🕐 在线时间</h4>
          <p>7×24 小时在线，随时为您服务</p>
        </div>
        <div class="sidebar-card">
          <h4>📦 常见问题</h4>
          <ul>
            <li>如何选择合适的家具尺寸？</li>
            <li>配送时效及运费说明</li>
            <li>退换货政策及流程</li>
            <li>实木家具日常保养</li>
          </ul>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { imgUrl } from "@/utils/img.js";
import { useBackNavigation } from "@/composables/useBackNavigation.js";

const { goBack } = useBackNavigation();

const CHAT_STORAGE_KEY = "aiChatMessages";
const inputMessage = ref("");
const messages = ref(loadMessages());
const loading = ref(false);
const bodyRef = ref(null);
const inputRef = ref(null);
const conversationId = ref(localStorage.getItem("aiConversationId") || "");

function loadMessages() {
  try {
    const saved = localStorage.getItem(CHAT_STORAGE_KEY);
    return saved ? JSON.parse(saved) : [];
  } catch { return []; }
}

function saveMessages() {
  try {
    localStorage.setItem(CHAT_STORAGE_KEY, JSON.stringify(messages.value));
  } catch { /* ignore quota */ }
}

// 商品卡片缓存 — 持久化到 localStorage
const PRODUCT_CACHE_KEY = "aiProductCache";
const productCache = ref(loadProductCache());
const cacheVersion = ref(0);

function loadProductCache() {
  try {
    const saved = localStorage.getItem(PRODUCT_CACHE_KEY);
    return saved ? JSON.parse(saved) : {};
  } catch { return {}; }
}

function saveProductCache() {
  try {
    localStorage.setItem(PRODUCT_CACHE_KEY, JSON.stringify(productCache.value));
  } catch { /* ignore */ }
}

const extractProductIds = (content) => {
  const ids = [];
  const regex = /\[商品:(\d+)\]/g;
  let match;
  while ((match = regex.exec(content)) !== null) {
    ids.push(parseInt(match[1]));
  }
  return [...new Set(ids)];
};

const loadProductInfo = async (ids) => {
  const missingIds = ids.filter((id) => !productCache.value[id]);
  if (missingIds.length === 0) return;
  const token = localStorage.getItem("token") || "";
  for (const id of missingIds) {
    try {
      const res = await fetch(`/api/furniture/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) {
        const json = await res.json();
        if (json.data) {
          productCache.value[id] = json.data;
        }
      }
    } catch (err) { /* ignore */ }
  }
  saveProductCache();
  cacheVersion.value++;
};

let abortController = null;

onMounted(() => {
  inputRef.value?.focus();
  // 恢复历史消息中的商品卡片
  if (messages.value.length > 0) {
    const allIds = new Set();
    messages.value.forEach(m => {
      extractProductIds(m.content).forEach(id => allIds.add(id));
    });
    if (allIds.size > 0) loadProductInfo([...allIds]);
  }
});

onBeforeUnmount(() => {
  if (abortController) abortController.abort();
});

const quickQuestions = ref([
  "有什么推荐的沙发？",
  "家具配送要多久？",
  "如何申请退换货？",
  "实木家具怎么保养？",
]);

const newChat = () => {
  messages.value = [];
  productCache.value = {};
  cacheVersion.value = 0;
  conversationId.value = "";
  localStorage.removeItem("aiConversationId");
  localStorage.removeItem(CHAT_STORAGE_KEY);
  localStorage.removeItem(PRODUCT_CACHE_KEY);
};

// 消息变化时自动持久化
watch(messages, saveMessages, { deep: true });

const scrollToBottom = () => {
  nextTick(() => {
    if (bodyRef.value) {
      bodyRef.value.scrollTop = bodyRef.value.scrollHeight;
    }
  });
};

const escapeHtml = (str) => {
  if (!str) return "";
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
};

const fmt = (content, _ver) => {
  if (!content) return "";
  const escaped = content
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
  let formatted = escaped
    .replace(/\n/g, "<br>")
    .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
    .replace(/\*(.*?)\*/g, "<em>$1</em>");
  // 替换 [商品:ID] 为可点击的商品卡片
  formatted = formatted.replace(
    /\[商品:(\d+)\]/g,
    (match, id) => {
      const product = productCache.value[id];
      const imgSrc = product?.fIcon ? imgUrl(product.fIcon) : null;
      if (product) {
        return `<a href="/furniture/detail/${id}" class="product-chip">
          <span class="pchip-thumb">
            ${imgSrc ? `<img src="${escapeHtml(imgSrc)}" onerror="this.style.display='none';this.nextElementSibling.style.display='flex'"/>` : ''}
            <span class="pchip-emoji" style="display:${imgSrc?'none':'flex'}">🪑</span>
          </span>
          <span class="pchip-name">${escapeHtml(product.fName || '')}</span>
          <span class="pchip-divider"></span>
          <span class="pchip-price">¥${escapeHtml(String(product.price || ''))}</span>
        </a>`;
      }
      return `<a href="/furniture/detail/${id}" class="product-chip">
        <span class="pchip-thumb"><span class="pchip-emoji">🪑</span></span>
        <span class="pchip-name">商品 #${id}</span>
      </a>`;
    }
  );
  return formatted;
};

const sendMessage = async (text) => {
  const msg = text || inputMessage.value.trim();
  if (!msg || loading.value) return;

  messages.value.push({ role: "user", content: msg });
  inputMessage.value = "";
  loading.value = true;
  scrollToBottom();

  try {
    if (abortController) abortController.abort();
    abortController = new AbortController();

    const response = await fetch("/api/ai/chat/stream", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
      body: JSON.stringify({
        message: msg,
        conversationId: conversationId.value || null,
      }),
      signal: abortController.signal,
    });

    if (!response.ok) throw new Error("请求失败");

    messages.value.push({ role: "assistant", content: "" });
    const lastIdx = messages.value.length - 1;

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = "";

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split("\n");
      buffer = lines.pop() || "";

      for (const line of lines) {
        // 兼容 "data: " 和 "data:" 两种格式
        let data = "";
        if (line.startsWith("data: ")) {
          data = line.slice(6).trim();
        } else if (line.startsWith("data:")) {
          data = line.slice(5).trim();
        }
        if (!data || data === "[DONE]") continue;

        try {
          const parsed = JSON.parse(data);
          if (parsed.type === "meta" && parsed.conversationId) {
            conversationId.value = parsed.conversationId;
            localStorage.setItem("aiConversationId", parsed.conversationId);
            continue;
          }
          if (parsed.content) {
            messages.value[lastIdx].content += parsed.content;
            scrollToBottom();
          }
          if (parsed.error) {
            messages.value[lastIdx].content = parsed.error;
          }
        } catch (e) {
          // 忽略解析失败的行
        }
      }
    }

    // 加载消息中引用的商品信息
    const allContent = messages.value
      .filter((m) => m.role === "assistant")
      .map((m) => m.content)
      .join(" ");
    const productIds = extractProductIds(allContent);
    if (productIds.length > 0) {
      await loadProductInfo(productIds);
      messages.value = [...messages.value];
    }
  } catch (e) {
    if (e.name !== "AbortError") {
      messages.value.push({
        role: "assistant",
        content: "抱歉，服务暂时不可用，请稍后再试。",
      });
    }
  } finally {
    loading.value = false;
    scrollToBottom();
  }
};
</script>

<style scoped>
.ai-chat-page {
  min-height: calc(100vh - 72px);
  background: linear-gradient(135deg, #f8f5f0 0%, #f0ebe3 100%);
}

/* ====== 面包屑导航 ====== */
.page-breadcrumb {
  max-width: 960px;
  margin: 0 auto;
  padding: 16px 24px 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #999;
}
.page-breadcrumb a { color: #999; text-decoration: none; }
.page-breadcrumb a:hover { color: #5a4a3a; }
.page-breadcrumb .current { color: #3d3226; }
.breadcrumb-back {
  display: flex; align-items: center; justify-content: center;
  width: 26px; height: 26px; border-radius: 50%;
  border: none; background: transparent;
  color: #999; cursor: pointer;
  transition: all .15s ease;
  margin-right: 6px; flex-shrink: 0;
}
.breadcrumb-back:hover { background: #e8e0d8; color: #5a4a3a; }

.chat-container {
  max-width: 960px;
  margin: 0 auto;
  padding: 0 24px 24px;
  display: grid;
  grid-template-columns: 1fr 260px;
  gap: 20px;
  height: calc(100vh - 140px);
}

/* ====== 左侧主聊天区 ====== */
.chat-main {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06), 0 4px 16px rgba(0,0,0,.04);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0ede8;
  flex-shrink: 0;
}

.header-left { display: flex; align-items: center; gap: 12px; }

.bot-avatar {
  width: 44px; height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #d4c5b2, #b8a088);
  display: flex; align-items: center; justify-content: center;
  font-size: 22px;
}

.header-text h2 { font-size: 16px; font-weight: 600; color: #3d3226; margin: 0; }
.header-text p  { font-size: 12px; color: #999; margin: 2px 0 0; }

.icon-btn {
  width: 36px; height: 36px;
  border: none; background: none;
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: #999;
  transition: all .15s;
}
.icon-btn:hover { background: #f5f3f0; color: #5a4a3a; }

/* ====== 消息区 ====== */
.chat-body {
  flex: 1; overflow-y: auto;
  padding: 20px;
  display: flex; flex-direction: column; gap: 16px;
}

.welcome-block { text-align: center; padding: 40px 20px; }
.welcome-emoji { font-size: 48px; margin-bottom: 12px; }
.welcome-block h3 { font-size: 18px; color: #3d3226; margin: 0 0 6px; font-weight: 600; }
.welcome-block p  { font-size: 14px; color: #999; margin: 0 0 16px; }

.quick-chips {
  display: flex; flex-wrap: wrap; gap: 8px; justify-content: center;
}

.chip {
  padding: 8px 16px;
  border: 1px solid #e8e0d8;
  border-radius: 20px;
  background: #faf8f5;
  font-size: 13px; color: #5a4a3a;
  cursor: pointer;
  transition: all .15s;
}
.chip:hover { background: #d4c5b2; border-color: #d4c5b2; color: #fff; }

.msg-row { display: flex; gap: 10px; }
.msg-row.user { flex-direction: row-reverse; }

.msg-avatar {
  width: 34px; height: 34px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; flex-shrink: 0;
}
.msg-row.assistant .msg-avatar { background: #f0ede8; }
.msg-row.user .msg-avatar       { background: #d4c5b2; }
.avatar-text { font-size: 11px; color: #5a4a3a; font-weight: 500; }

.msg-body { max-width: 75%; }

.msg-bubble {
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 14px; line-height: 1.55;
  color: #3d3226;
  word-break: break-word;
  overflow-wrap: break-word;
}
.msg-row.assistant .msg-bubble { background: #f5f3f0; border-bottom-left-radius: 4px; }
.msg-row.user .msg-bubble       { background: #3d3226; color: #fff; border-bottom-right-radius: 4px; }

.msg-bubble.thinking { padding: 14px 24px; background: #f5f3f0; }
.msg-bubble :deep(strong) { font-weight: 600; color: #b8844a; }
.msg-row.user .msg-bubble :deep(strong) { color: #e8c876; }

.typing-dots {
  display: flex; gap: 4px; padding: 4px 0;
}
.typing-dots i {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #ccc;
  animation: dot 1.4s infinite ease-in-out both;
}
.typing-dots i:nth-child(1) { animation-delay: 0s; }
.typing-dots i:nth-child(2) { animation-delay: .16s; }
.typing-dots i:nth-child(3) { animation-delay: .32s; }

@keyframes dot { 0%,80%,100% { opacity: .25; } 40% { opacity: 1; } }

/* ====== 输入区 ====== */
.chat-footer {
  padding: 12px 20px;
  border-top: 1px solid #f0ede8;
  flex-shrink: 0;
}

.input-row {
  display: flex; align-items: center; gap: 8px;
  background: #f5f3f0; border-radius: 12px;
  padding: 4px;
}

.input-row input {
  flex: 1; border: none; background: transparent;
  padding: 10px 12px; font-size: 14px;
  outline: none; color: #3d3226;
}
.input-row input::placeholder { color: #bbb; }

.send-btn {
  width: 40px; height: 40px;
  border: none;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  background: #b8844a; color: #fff;
  transition: all .15s;
  flex-shrink: 0;
}
.send-btn:hover:not(:disabled)   { background: #a0703c; }
.send-btn:disabled { background: #ddd; color: #aaa; cursor: default; }

/* ====== 右侧侧边栏 ====== */
.chat-sidebar {
  display: flex; flex-direction: column; gap: 12px;
}

.sidebar-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
}

.sidebar-card h4 { font-size: 14px; font-weight: 600; color: #3d3226; margin: 0 0 8px; }
.sidebar-card p  { font-size: 13px; color: #888; margin: 0; line-height: 1.5; }
.sidebar-card ul { margin: 0; padding: 0 0 0 16px; }
.sidebar-card li { font-size: 13px; color: #666; margin-bottom: 6px; line-height: 1.5; }

.first-loading { text-align: center; padding: 20px; }

/* ====== 响应式 ====== */
@media (max-width: 768px) {
  .ai-chat-page { padding: 0; }
  .page-breadcrumb { padding: 12px 16px 6px; }
  .chat-container {
    grid-template-columns: 1fr;
    height: calc(100vh - 110px);
    padding: 0 12px 12px;
  }
  .chat-sidebar { display: none; }
}
</style>

<!-- v-html 卡片样式，不能 scoped -->
<style>
.product-chip {
  display: inline-flex !important;
  align-items: center;
  gap: 5px;
  height: 34px;
  padding: 0 10px 0 3px;
  margin: 2px 4px 2px 0;
  background: #fff;
  border: 1px solid #e4dbcf;
  border-radius: 18px;
  cursor: pointer;
  transition: all .15s ease;
  text-decoration: none;
  color: #3d3226;
  vertical-align: middle;
}
.product-chip:hover {
  border-color: #c8a882;
  box-shadow: 0 1px 6px rgba(153,112,62,.07);
}
.pchip-thumb {
  position: relative;
  width: 26px; height: 26px;
  border-radius: 14px;
  overflow: hidden;
  flex-shrink: 0;
}
.pchip-thumb img {
  width: 26px; height: 26px;
  object-fit: cover;
  display: block;
}
.pchip-emoji {
  position: absolute; inset: 0;
  background: #f4efe8;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px;
}
.pchip-name {
  font-size: 12.5px; font-weight: 500;
  color: #4a382a;
  max-width: 96px;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.pchip-divider {
  width: 1px; height: 14px;
  background: #e6ddd0;
  flex-shrink: 0;
}
.pchip-price {
  font-size: 12.5px; font-weight: 600;
  color: #a86e3a;
  white-space: nowrap;
}
</style>

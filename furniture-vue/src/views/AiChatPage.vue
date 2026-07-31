<template>
  <div class="ai-chat-page">
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
              <div class="msg-bubble" v-html="fmt(msg.content)"></div>
              <span v-if="msg.role === 'assistant' && i === messages.length - 1 && loading" class="typing-dots">
                <i></i><i></i><i></i>
              </span>
            </div>
          </div>

          <div v-if="loading && messages.length === 0" class="first-loading">
            <div class="typing-dots"><i></i><i></i><i></i></div>
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
import { nextTick, onBeforeUnmount, onMounted, ref } from "vue";

const inputMessage = ref("");
const messages = ref([]);
const loading = ref(false);
const bodyRef = ref(null);
const inputRef = ref(null);
const conversationId = ref(localStorage.getItem("aiConversationId") || "");

let abortController = null;

onMounted(() => {
  inputRef.value?.focus();
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
  conversationId.value = "";
  localStorage.removeItem("aiConversationId");
};

const scrollToBottom = () => {
  nextTick(() => {
    if (bodyRef.value) {
      bodyRef.value.scrollTop = bodyRef.value.scrollHeight;
    }
  });
};

const fmt = (content) => {
  if (!content) return "";
  const escaped = content
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
  return escaped
    .replace(/\n/g, "<br>")
    .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
    .replace(/\*(.*?)\*/g, "<em>$1</em>");
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
        if (line.startsWith("data:")) {
          const data = line.slice(5).trim();
          if (data === "[DONE]") break;
          if (data.startsWith("[CID]")) {
            conversationId.value = data.slice(5).trim();
            localStorage.setItem("aiConversationId", conversationId.value);
            break;
          }
          messages.value[lastIdx].content += data;
          scrollToBottom();
        }
      }
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
  padding: 24px;
}

.chat-container {
  max-width: 960px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 260px;
  gap: 20px;
  height: calc(100vh - 120px);
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
}
.msg-row.assistant .msg-bubble { background: #f5f3f0; border-bottom-left-radius: 4px; }
.msg-row.user .msg-bubble       { background: #3d3226; color: #fff; border-bottom-right-radius: 4px; }

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
  .ai-chat-page { padding: 12px; }
  .chat-container {
    grid-template-columns: 1fr;
    height: calc(100vh - 96px);
  }
  .chat-sidebar { display: none; }
}
</style>

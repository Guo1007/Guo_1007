<template>
  <div class="ai-chat-wrapper" :style="wrapperStyle">
    <!-- 悬浮按钮 -->
    <div class="chat-trigger" :class="{ active: isOpen }"
         @mousedown="startDrag" @touchstart="startDrag"
         @click="handleTriggerClick">
      <el-icon :size="28">
        <ChatDotRound/>
      </el-icon>
      <span v-if="!isOpen" class="trigger-text">智能客服</span>
    </div>

    <!-- 聊天窗口 -->
    <Transition name="chat-slide">
      <div v-if="isOpen" class="chat-window" :style="chatWindowStyle">
        <!-- 头部 -->
        <div class="chat-header">
          <div class="header-left">
            <div class="bot-avatar">
              <el-icon :size="20">
                <Monitor/>
              </el-icon>
            </div>
            <div class="header-info">
              <span class="bot-name">小智 - AI客服</span>
              <span class="bot-status">
                <span class="status-dot"></span>在线
              </span>
            </div>
          </div>
          <div class="header-actions">
            <el-icon class="action-icon" @click="clearChat">
              <Delete/>
            </el-icon>
            <el-icon class="action-icon" @click="isOpen = false">
              <Close/>
            </el-icon>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="chat-messages" ref="messagesRef">
          <!-- 欢迎消息 -->
          <div class="message-item assistant">
            <div class="message-avatar">
              <el-icon :size="16">
                <Monitor/>
              </el-icon>
            </div>
            <div class="message-content">
              <div class="message-bubble">
                <p>你好！我是家具商城的智能客服小智 🛋️</p>
                <p>有什么可以帮您的吗？</p>
              </div>
            </div>
          </div>

          <!-- 消息列表 -->
          <div v-for="(msg, index) in messages" :key="index"
               class="message-item" :class="msg.role">
            <div v-if="msg.role === 'assistant'" class="message-avatar">
              <el-icon :size="16">
                <Monitor/>
              </el-icon>
            </div>
            <div class="message-content">
              <div class="message-bubble" v-html="formatMessage(msg.content)"></div>
              <div v-if="msg.role === 'assistant' && index === messages.length - 1 && loading" class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>

          <!-- 加载中 -->
          <div v-if="loading" class="message-item assistant">
            <div class="message-avatar">
              <el-icon :size="16">
                <Monitor/>
              </el-icon>
            </div>
            <div class="message-content">
              <div class="message-bubble loading">
                <div class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 快捷问题 -->
        <div v-if="messages.length === 0" class="quick-questions">
          <div class="question-chip" v-for="q in quickQuestions" :key="q" @click="sendMessage(q)">
            {{ q }}
          </div>
        </div>

        <!-- 输入框 -->
        <div class="chat-input">
          <el-input
              v-model="inputMessage"
              placeholder="输入您的问题..."
              :disabled="loading"
              @keyup.enter="sendMessage()"
              size="large"
          >
            <template #append>
              <el-button :icon="Promotion" @click="sendMessage()" :loading="loading"/>
            </template>
          </el-input>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import {computed, nextTick, onMounted, onUnmounted, ref, watch} from 'vue'
import {ChatDotRound, Close, Delete, Monitor, Promotion} from '@element-plus/icons-vue'

const isOpen = ref(false)
const inputMessage = ref('')
const messages = ref([])
const loading = ref(false)
const messagesRef = ref(null)

// ========== 拖拽定位 ==========
const position = ref({x: 24, y: 0})
const isDragging = ref(false)
const hasMoved = ref(false)
const dragStart = ref({x: 0, y: 0})
const posStart = ref({x: 0, y: 0})

onMounted(() => {
  const saved = localStorage.getItem('aiChatPosition')
  if (saved) {
    try {
      position.value = JSON.parse(saved)
    } catch (e) {
      setDefaultPosition()
    }
  } else {
    setDefaultPosition()
  }
})

const setDefaultPosition = () => {
  position.value = {
    x: 24,
    y: window.innerHeight - 100
  }
}

const wrapperStyle = computed(() => ({
  left: position.value.x + 'px',
  top: position.value.y + 'px',
  right: 'auto',
  bottom: 'auto'
}))

// 聊天窗口在触发按钮的上方，根据位置自动判断展开方向
const chatWindowStyle = computed(() => {
  // 如果按钮在屏幕左半边，窗口向右展开；否则向左展开
  const isLeftSide = position.value.x < window.innerWidth / 2
  if (isLeftSide) {
    return {left: '0', right: 'auto'}
  } else {
    return {left: 'auto', right: '0'}
  }
})

const startDrag = (e) => {
  isDragging.value = true
  hasMoved.value = false
  const clientX = e.touches ? e.touches[0].clientX : e.clientX
  const clientY = e.touches ? e.touches[0].clientY : e.clientY
  dragStart.value = {x: clientX, y: clientY}
  posStart.value = {...position.value}
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.addEventListener('touchmove', onDrag, {passive: false})
  document.addEventListener('touchend', stopDrag)
}

const onDrag = (e) => {
  if (!isDragging.value) return
  const clientX = e.touches ? e.touches[0].clientX : e.clientX
  const clientY = e.touches ? e.touches[0].clientY : e.clientY
  const dx = Math.abs(clientX - dragStart.value.x)
  const dy = Math.abs(clientY - dragStart.value.y)
  if (dx > 5 || dy > 5) {
    hasMoved.value = true
  }
  if (hasMoved.value) {
    e.preventDefault()
    let newX = posStart.value.x + (clientX - dragStart.value.x)
    let newY = posStart.value.y + (clientY - dragStart.value.y)
    newX = Math.max(10, Math.min(newX, window.innerWidth - 80))
    newY = Math.max(10, Math.min(newY, window.innerHeight - 80))
    position.value = {x: newX, y: newY}
  }
}

const stopDrag = () => {
  if (isDragging.value) {
    isDragging.value = false
    if (hasMoved.value) {
      localStorage.setItem('aiChatPosition', JSON.stringify(position.value))
    }
    cleanupListeners()
  }
}

const cleanupListeners = () => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', stopDrag)
}

onUnmounted(() => {
  cleanupListeners()
})

// 点击触发按钮 —— 只有没拖动过才切换聊天窗口
const handleTriggerClick = () => {
  if (hasMoved.value) {
    hasMoved.value = false
    return
  }
  isOpen.value = !isOpen.value
}

const quickQuestions = ref([
  '有什么推荐的沙发？',
  '家具配送要多久？',
  '如何申请退换货？',
  '实木家具怎么保养？'
])

// 清空聊天记录
const clearChat = () => {
  messages.value = []
}

// 格式化消息（简单的markdown支持）
const formatMessage = (content) => {
  if (!content) return ''
  return content
      .replace(/\n/g, '<br>')
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 发送消息
const sendMessage = async (text) => {
  const message = text || inputMessage.value.trim()
  if (!message || loading.value) return

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: message
  })
  inputMessage.value = ''
  loading.value = true
  scrollToBottom()

  try {
    // 使用EventSource实现SSE
    const response = await fetch('/api/ai/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
      },
      body: JSON.stringify({
        message: message,
        history: messages.value.slice(-10).map(m => ({
          role: m.role,
          content: m.content
        }))
      })
    })

    if (!response.ok) {
      messages.value.push({
        role: 'assistant',
        content: `请求失败: ${response.status}`
      })
      return
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let assistantMessage = ''

    // 添加空的assistant消息
    messages.value.push({
      role: 'assistant',
      content: ''
    })

    while (true) {
      const {done, value} = await reader.read()
      if (done) break

      const chunk = decoder.decode(value, {stream: true})
      const lines = chunk.split('\n')

      for (const line of lines) {
        // 兼容 data: 和 data: (有空格) 两种格式
        let data = ''
        if (line.startsWith('data: ')) {
          data = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          data = line.slice(5).trim()
        }

        if (!data || data === '[DONE]') continue

        try {
          const parsed = JSON.parse(data)
          if (parsed.content) {
            assistantMessage += parsed.content
            messages.value[messages.value.length - 1].content = assistantMessage
            scrollToBottom()
          }
          if (parsed.error) {
            messages.value[messages.value.length - 1].content = parsed.error
          }
        } catch (e) {
          // 忽略解析错误
        }
      }
    }

    // 如果没有收到任何响应
    if (!assistantMessage) {
      messages.value[messages.value.length - 1].content = '抱歉，暂时无法获取回复，请稍后再试。'
    }

  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: '抱歉，服务暂时不可用，请稍后再试。'
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

watch(messages, () => {
  scrollToBottom()
}, {deep: true})
</script>

<style scoped>
.ai-chat-wrapper {
  position: fixed;
  z-index: 9999;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 悬浮按钮 */
.chat-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 20px;
  background: linear-gradient(135deg, #3e4e49 0%, #3e4e49 100%);
  border-radius: 50px;
  cursor: grab;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  transition: box-shadow 0.3s ease, transform 0.3s ease;
  color: white;
  user-select: none;
}

.chat-trigger:active {
  cursor: grabbing;
}

.chat-trigger:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.5);
}

.chat-trigger.active {
  background: linear-gradient(135deg, #3e4e49 0%, #3e4e49 100%);
  border-radius: 50%;
  padding: 16px;
}

.trigger-text {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

/* 聊天窗口 */
.chat-window {
  position: absolute;
  bottom: 70px;
  width: 400px;
  height: 560px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e8e8e8;
}

/* 动画 */
.chat-slide-enter-active,
.chat-slide-leave-active {
  transition: all 0.3s ease;
}

.chat-slide-enter-from,
.chat-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

/* 头部 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #515260 0%, #515260 100%);
  color: white;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bot-avatar {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-info {
  display: flex;
  flex-direction: column;
}

.bot-name {
  font-size: 15px;
  font-weight: 600;
}

.bot-status {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0.9;
}

.status-dot {
  width: 6px;
  height: 6px;
  background: #52c41a;
  border-radius: 50%;
  display: inline-block;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.action-icon {
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.action-icon:hover {
  opacity: 1;
}

/* 消息列表 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f7f8fa;
}

.message-item {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.message-content {
  max-width: 75%;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.message-item.assistant .message-bubble {
  background: white;
  color: #333;
  border-top-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.message-item.user .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-top-right-radius: 4px;
}

/* 打字动画 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  background: #999;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-4px);
  }
}

/* 快捷问题 */
.quick-questions {
  padding: 12px 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  background: #f7f8fa;
  border-top: 1px solid #eee;
}

.question-chip {
  padding: 6px 12px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.question-chip:hover {
  border-color: #667eea;
  color: #667eea;
  background: #f0f2ff;
}

/* 输入框 */
.chat-input {
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #eee;
}

.chat-input :deep(.el-input-group__append) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
}

.chat-input :deep(.el-input-group__append:hover) {
  opacity: 0.9;
}

/* 滚动条 */
.chat-messages::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 2px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}
</style>
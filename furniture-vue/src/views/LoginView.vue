<template>

  <div class="login-container" @mousemove="onMouseMove">
    <div class="left-panel" :class="{ 'slide-in-left': isLoaded }">
      <div class="decor-grid"></div>
      <div class="brand">
        <div class="brand-icon">
          <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
            <rect width="28" height="28" rx="7" fill="white" fillOpacity="0.15"/>
            <path d="M7 14L12 9L17 14L12 19L7 14Z" fill="white" fillOpacity="0.9"/>
            <path d="M13 14L18 9L21 12V16L18 19L13 14Z" fill="white" fillOpacity="0.5"/>
          </svg>
        </div>
        <span>家具商城</span>
      </div>

      <div class="flex-1 flex items-end justify-center" style="position:relative; z-index:20;">
        <div style="width:550px; height:500px; position:relative;">
          <AnimatedCharacters
              :mouseX="mouseX"
              :mouseY="mouseY"
              :isTyping="isTyping"
              :showPassword="showPasswordVisible"
          />
        </div>
      </div>
    </div>

    <div class="right-panel" :class="{ 'slide-in-right': isLoaded }">
      <div class="auth-box">
        <div class="auth-header">
          <h1>登录</h1>
        </div>
        <div class="auth-tabs">
          <button :class="['tab-btn', { active: loginType === 'code' }]" @click="loginType = 'code'">
            验证码登录
          </button>
          <button :class="['tab-btn', { active: loginType === 'password' }]" @click="loginType = 'password'">
            密码登录
          </button>
        </div>

        <!-- 表单 -->
        <form class="auth-form" @submit.prevent="handleLogin">
          <!-- 账号 -->
          <div class="form-group">
            <label>账号</label>
            <div class="input-wrapper">
              <svg class="input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <input
                  v-model="form.account"
                  type="text"
                  placeholder="输入您的账号"
                  @focus="onInputFocus"
                  @blur="onInputBlur"
              />
            </div>
            <span class="error-msg" v-if="errors.account">{{ errors.account }}</span>
          </div>

          <!-- 验证码登录 -->
          <template v-if="loginType === 'code'">
            <div class="form-group">
              <label>验证码</label>
              <div class="input-wrapper code-wrapper">
                <svg class="input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                     stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
                <input
                    v-model="form.code"
                    type="text"
                    placeholder="输入验证码"
                    maxlength="6"
                    @focus="onInputFocus"
                    @blur="onInputBlur"
                />
                <button
                    type="button"
                    class="code-btn"
                    :disabled="codeCountdown > 0 || !form.account"
                    @click="sendVerifyCode"
                >{{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
                </button>
              </div>
            </div>
          </template>

          <!-- 密码登录 -->
          <template v-else>
            <div class="form-group">
              <label>密码</label>
              <div class="input-wrapper">
                <svg class="input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                     stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
                <input
                    v-model="form.password"
                    :type="showPasswordVisible ? 'text' : 'password'"
                    placeholder="输入您的密码"
                    @focus="onInputFocus"
                    @blur="onInputBlur"
                />
                <button type="button" class="toggle-password" @click="togglePassword">
                  <template v-if="!showPasswordVisible">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                         stroke-linecap="round" stroke-linejoin="round">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                      <circle cx="12" cy="12" r="3"></circle>
                    </svg>
                  </template>
                  <template v-else>
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                         stroke-linecap="round" stroke-linejoin="round">
                      <path
                          d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                      <line x1="1" y1="1" x2="23" y2="23"></line>
                    </svg>
                  </template>
                </button>
              </div>
              <div class="form-actions">
                <router-link to="/forgot-password" class="link-text">忘记密码？</router-link>
                <router-link to="/register" class="link-text">没有账号？立即注册</router-link>
              </div>
            </div>
          </template>

          <!-- 提交按钮 -->
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>

      </div>
    </div>
  </div>
</template>

<script setup>

import {onBeforeUnmount, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {getUserInfo, login, sendCode} from '@/api/user.js'
import {validateEmail, validatePhone} from '@/utils/validators.js'
import {useCartStore} from '@/stores/cart.js'
import AnimatedCharacters from '@/components/AnimatedCharacters.vue'

const router = useRouter()

// ========== 表单状态 ==========
const loginType = ref('code')
const form = ref({account: '', code: '', password: ''})
const errors = ref({account: ''})
const loading = ref(false)
const codeCountdown = ref(0)
const countdownTimer = ref(null)

// ========== 动画状态 ==========
const mouseX = ref(0)
const mouseY = ref(0)
const isTyping = ref(false)
const showPasswordVisible = ref(false)
const isLoaded = ref(false)

// ========== 载入动画 ==========
onMounted(() => {
  // 延迟一帧触发入场动画，确保DOM已渲染
  requestAnimationFrame(() => {
    isLoaded.value = true
  })
})

// ========== 鼠标监听 ==========
function onMouseMove(e) {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}

// ========== 输入框聚焦状态 ==========
function onInputFocus() {
  isTyping.value = true
}

function onInputBlur() {
  isTyping.value = false
}

// ========== 密码可见切换 ==========
function togglePassword() {
  showPasswordVisible.value = !showPasswordVisible.value
}

// ========== 表单验证 ==========
function isEmail(val) {
  return val && val.includes('@')
}

function validateAccount() {
  const val = form.value.account
  if (!val) {
    errors.value.account = '请输入邮箱或手机号'
    return false
  }
  if (isEmail(val)) {
    const err = validateEmail(val)
    errors.value.account = err
    return !err
  } else {
    const err = validatePhone(val)
    errors.value.account = err
    return !err
  }
}

// ========== 发送验证码 ==========
async function sendVerifyCode() {
  if (!validateAccount()) {
    ElMessage.warning(errors.value.account || '请输入正确的邮箱或手机号')
    return
  }
  try {
    const res = await sendCode({account: form.value.account})
    if (res.success) {
      startCountdown()
      const tip = isEmail(form.value.account)
          ? '验证码已发送至邮箱，请注意查收'
          : '验证码已发送，请注意查收'
      ElMessage.success(tip)
    } else {
      ElMessage.error(res.errorMsg || '发送失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '发送失败，请重试')
  }
}

// ========== 倒计时 ==========
function startCountdown() {
  codeCountdown.value = 60
  countdownTimer.value = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      clearInterval(countdownTimer.value)
    }
  }, 1000)
}

// ========== 登录处理 ==========
async function handleLogin() {
  if (!validateAccount()) {
    ElMessage.warning(errors.value.account || '请输入正确的邮箱或手机号')
    return
  }
  if (loginType.value === 'code' && !form.value.code) {
    ElMessage.warning('请输入验证码')
    return
  }
  if (loginType.value === 'password' && !form.value.password) {
    ElMessage.warning('请输入密码')
    return
  }

  loading.value = true
  try {
    const loginData = {
      account: form.value.account,
      code: loginType.value === 'code' ? form.value.code : undefined,
      passWord: loginType.value === 'password' ? form.value.password : undefined
    }
    const res = await login(loginData)
    if (res.success) {
      const token = res.data
      localStorage.setItem('token', token)
      ElMessage.success('登录成功！正在获取用户信息...')
      try {
        const userRes = await getUserInfo()
        if (userRes.success) {
          const userInfo = userRes.data
          localStorage.setItem('userInfo', JSON.stringify(userInfo))
          localStorage.setItem('userName', userInfo.userName || '用户')
          localStorage.setItem('userIcon', userInfo.icon || '/images/default-avatar.png')
          const cartStore = useCartStore()
          cartStore.reloadCart()
          ElMessage.success(`欢迎回来，${userInfo.userName || '用户'}！`)
        } else {
          ElMessage.warning('获取用户信息失败，但登录成功')
        }
      } catch (userError) {
        ElMessage.warning('获取用户信息失败，但登录成功')
      }
      setTimeout(() => {
        router.push('/')
      }, 800)
    } else {
      ElMessage.error(res.errorMsg || '登录失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

onBeforeUnmount(() => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
})
</script>

<style scoped>
@keyframes slideInLeft {
  0% {
    transform: translateX(-100%);
  }
  50% {
    /* 到达中间位置，准备碰撞 */
    transform: translateX(5%);
  }
  65% {
    /* 碰撞瞬间 - 被压缩（果冻变形） */
    transform: translateX(0%) scaleX(0.95);
  }
  75% {
    /* 反弹膨胀 */
    transform: translateX(0%) scaleX(1.03);
  }
  85% {
    /* 再次微弹 */
    transform: translateX(0%) scaleX(0.99);
  }
  100% {
    transform: translateX(0%) scaleX(1);
  }
}

@keyframes slideInRight {
  0% {
    transform: translateX(100%);
  }
  50% {
    /* 到达中间位置，准备碰撞 */
    transform: translateX(-5%);
  }
  65% {
    /* 碰撞瞬间 - 被压缩（果冻变形） */
    transform: translateX(0%) scaleX(0.95);
  }
  75% {
    /* 反弹膨胀 */
    transform: translateX(0%) scaleX(1.03);
  }
  85% {
    /* 再次微弹 */
    transform: translateX(0%) scaleX(0.99);
  }
  100% {
    transform: translateX(0%) scaleX(1);
  }
}

/* 左侧入场 */
.left-panel.slide-in-left {
  animation: slideInLeft 1.2s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

/* 右侧入场 */
.right-panel.slide-in-right {
  animation: slideInRight 1.2s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

/* ===== 整体布局 - 左右50/50分栏 ===== */
.login-container {
  position: fixed;
  inset: 0;
  display: flex;
  overflow: hidden;
}

/* ===== 左侧面板 - 深蓝渐变 + 网格线 + 装饰光晕 ===== */
.left-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(145deg, #3a4a5a 0%, #5a6a7a 50%, #4a5a6a 100%);
  /* 初始状态：隐藏在左侧 */
  transform: translateX(-100%);
}

/* 装饰光晕1 */
.left-panel::before {
  content: '';
  position: absolute;
  top: 15%;
  right: 10%;
  width: 300px;
  height: 300px;
  background: rgba(217, 138, 74, 0.20);
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
  z-index: 0;
}

/* 装饰光晕2 */
.left-panel::after {
  content: '';
  position: absolute;
  bottom: 10%;
  left: 5%;
  width: 400px;
  height: 400px;
  background: rgba(180, 120, 60, 0.25);
  border-radius: 50%;
  filter: blur(100px);
  pointer-events: none;
  z-index: 0;
}

/* 网格线装饰 */
.decor-grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
  linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
  z-index: 1;
}

/* 品牌区域 */
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
  position: relative;
  z-index: 20;
}

.brand-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  backdrop-filter: blur(8px);
}

/* 卡通角色区域 */
.characters-area {
  width: 100%;
  max-width: 420px;
  height: 340px;
  position: relative;
  margin: 0 auto;
}

/* 底部链接 */
.footer-links {
  display: flex;
  gap: 24px;
  color: rgba(255, 255, 255, 0.45);
  font-size: 13px;
  position: relative;
  z-index: 20;
}

.footer-links a {
  color: inherit;
  text-decoration: none;
  transition: color 0.2s;
}

.footer-links a:hover {
  color: rgba(255, 255, 255, 0.9);
}

/* ===== 右侧面板 - 白色背景 ===== */
.right-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  padding: 32px;
  /* 初始状态：隐藏在右侧 */
  transform: translateX(100%);
}

.auth-box {
  width: 100%;
  max-width: 400px;
}

/* 标题区域 */
.auth-header {
  text-align: center;
  margin-bottom: 40px;
}

/* ===== 登录类型切换 ===== */
.auth-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 3px;
}

.tab-btn {
  flex: 1;
  padding: 10px 16px;
  background: none;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-btn.active {
  background: white;
  color: #111827;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  font-weight: 600;
}

.tab-btn:hover:not(.active) {
  color: #374151;
}

.auth-header h1 {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 12px 0;
  letter-spacing: -0.3px;
}

.subtitle {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
  letter-spacing: 0.2px;
}

/* ===== 表单 ===== */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  letter-spacing: 0.2px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  height: 48px;
  background: #fafafa;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s ease;
}

.input-wrapper:hover {
  border-color: #3b82f6;
}

.input-wrapper:focus-within {
  border-color: #1e40af;
  background: white;
  box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.08);
}

.input-icon {
  margin-left: 14px;
  color: #b0b7c3;
  flex-shrink: 0;
}

.input-wrapper input {
  flex: 1;
  height: 100%;
  padding: 0 14px 0 12px;
  border: none;
  background: none;
  font-size: 14px;
  color: #111827;
  outline: none;
}

.input-wrapper input::placeholder {
  color: #c0c4cc;
  font-size: 14px;
}

/* 密码眼睛按钮 */
.toggle-password {
  padding: 10px 14px;
  background: none;
  border: none;
  cursor: pointer;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
}

.toggle-password:hover {
  color: #374151;
}

/* 验证码 */
.code-wrapper {
  position: relative;
}

.code-wrapper input {
  padding-right: 110px;
}

.code-btn {
  position: absolute;
  right: 6px;
  padding: 8px 14px;
  background: transparent;
  color: #1e40af;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.code-btn:hover:not(:disabled) {
  background: rgba(30, 64, 175, 0.06);
  color: #1d4ed8;
}

.code-btn:disabled {
  color: #b0b7c3;
  cursor: not-allowed;
}

.error-msg {
  font-size: 13px;
  color: #dc2626;
  margin-top: 4px;
}

.form-actions {
  display: flex;
  justify-content: space-between;
}

.link-text {
  font-size: 13px;
  color: #1e40af;
  font-weight: 500;
  text-decoration: none;
  cursor: pointer;
}

.link-text:hover {
  text-decoration: underline;
  color: #1d4ed8;
}

/* ===== 登录按钮 - 深蓝色 ===== */
.submit-btn {
  width: 100%;
  height: 48px;
  background: #1e40af;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  letter-spacing: 1px;
}

.submit-btn:hover:not(:disabled) {
  background: #1d4ed8;
  border-color: #1d4ed8;
}

.submit-btn:active:not(:disabled) {
  transform: scale(0.99);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ===== 分割线 ===== */
.divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 20px 0 0;
  color: #d1d5db;
  font-size: 13px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #e5e7eb;
}

.divider span {
  color: #9ca3af;
  white-space: nowrap;
  padding: 0;
}

/* ===== 飞书按钮 ===== */
.feishu-btn {
  width: 100%;
  height: 48px;
  background: white;
  color: #374151;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
}

.feishu-btn:hover {
  background: #eff6ff;
  border-color: rgba(30, 64, 175, 0.25);
  color: #1e40af;
}

/* ===== 底部提示 ===== */
.auth-footer {
  margin-top: 28px;
  text-align: center;
}

.auth-footer p {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

.auth-footer a {
  color: #1e40af;
  font-weight: 500;
  text-decoration: none;
  cursor: pointer;
}

.auth-footer a:hover {
  text-decoration: underline;
  color: #1d4ed8;
}

/* ===== 响应式 ===== */
@media (max-width: 1024px) {
  .login-container {
    flex-direction: column;
  }

  .left-panel {
    display: none;
  }

  .right-panel {
    padding: 32px 24px;
  }
}

@media (max-width: 640px) {
  .auth-box {
    max-width: 100%;
  }
}
</style>
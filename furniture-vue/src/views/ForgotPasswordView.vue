<template>

  <div class="login-container" @mousemove="onMouseMove">
    <div class="left-panel" :class="{ 'slide-in-left': isLoaded }">
      <div class="decor-grid"></div>
      <div class="brand">
        <div class="brand-icon">
          <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
            <rect width="28" height="28" rx="7" fill="white" fill-opacity="0.15"/>
            <rect x="4" y="5" width="20" height="8" rx="2" fill="white" fill-opacity="0.85"/>
            <rect x="3" y="13" width="22" height="5" rx="2.5" fill="white" fill-opacity="0.95"/>
            <rect x="6" y="18" width="2.5" height="5" rx="1.25" fill="#D98A4A" fill-opacity="0.8"/>
            <rect x="19.5" y="18" width="2.5" height="5" rx="1.25" fill="#D98A4A" fill-opacity="0.8"/>
          </svg>
        </div>
        <div class="brand-text">
          <span class="brand-name">家具商城</span>
          <span class="brand-slogan">品质家居 · 用心打造</span>
        </div>
      </div>

      <div class="flex-1 flex items-end justify-center" style="position:relative; z-index:20;">
        <div style="width:550px; height:500px; position:relative;">
          <AnimatedCharacters
              :mouseX="mouseX"
              :mouseY="mouseY"
              :isTyping="isTyping"
              :showPassword="showAnyPassword"
          />
        </div>
      </div>
    </div>

    <div class="right-panel" :class="{ 'slide-in-right': isLoaded }">
      <div class="auth-box">
        <div class="auth-header">
          <h1>重置密码</h1>
          <p class="subtitle">请使用注册邮箱验证身份后设置新密码</p>
        </div>

        <form class="auth-form" @submit.prevent="handleSubmit">
          <!-- 邮箱 -->
          <div class="form-group">
            <label>注册邮箱</label>
            <div class="input-wrapper">
              <svg class="input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="4" width="20" height="16" rx="2"/>
                <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/>
              </svg>
              <input
                  v-model="form.email"
                  type="text"
                  placeholder="输入注册时使用的邮箱"
                  @focus="onInputFocus"
                  @blur="onInputBlur"
              />
            </div>
            <span class="error-msg" v-if="errors.email">{{ errors.email }}</span>
          </div>

          <!-- 验证码 -->
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
                  :disabled="codeCountdown > 0 || !form.email"
                  @click="sendCode"
              >{{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
              </button>
            </div>
            <span class="error-msg" v-if="errors.code">{{ errors.code }}</span>
          </div>

          <!-- 新密码 -->
          <div class="form-group">
            <label>新密码</label>
            <div class="input-wrapper">
              <svg class="input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <input
                  v-model="form.password"
                  :type="showPassword1 ? 'text' : 'password'"
                  placeholder="输入新密码（6位以上，含大小写字母和数字）"
                  @focus="onInputFocus"
                  @blur="onInputBlur"
              />
              <button type="button" class="toggle-password" @click="showPassword1 = !showPassword1">
                <template v-if="!showPassword1">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                       stroke-linecap="round" stroke-linejoin="round">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                </template>
                <template v-else>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                       stroke-linecap="round" stroke-linejoin="round">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                    <line x1="1" y1="1" x2="23" y2="23"></line>
                  </svg>
                </template>
              </button>
            </div>
            <span class="error-msg" v-if="errors.password">{{ errors.password }}</span>
          </div>

          <!-- 确认密码 -->
          <div class="form-group">
            <label>确认密码</label>
            <div class="input-wrapper">
              <svg class="input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <input
                  v-model="form.confirmPassword"
                  :type="showPassword2 ? 'text' : 'password'"
                  placeholder="请再次输入新密码"
                  @focus="onInputFocus"
                  @blur="onInputBlur"
              />
              <button type="button" class="toggle-password" @click="showPassword2 = !showPassword2">
                <template v-if="!showPassword2">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                       stroke-linecap="round" stroke-linejoin="round">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                </template>
                <template v-else>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                       stroke-linecap="round" stroke-linejoin="round">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                    <line x1="1" y1="1" x2="23" y2="23"></line>
                  </svg>
                </template>
              </button>
            </div>
            <span class="error-msg" v-if="errors.confirmPassword">{{ errors.confirmPassword }}</span>
          </div>

          <!-- 提交 -->
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '提交中...' : '重置密码' }}
          </button>
        </form>

        <div class="auth-footer">
          <p>想起密码了？<router-link to="/login">返回登录</router-link></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>

import {computed, onBeforeUnmount, onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {sendResetCode, resetPassword} from '@/api/user.js'
import {validateConfirmPassword, validateEmail, validatePassword} from '@/utils/validators.js'
import AnimatedCharacters from '@/components/AnimatedCharacters.vue'

const router = useRouter()

const form = reactive({
  email: '',
  code: '',
  password: '',
  confirmPassword: ''
})
const errors = reactive({
  email: '',
  code: '',
  password: '',
  confirmPassword: ''
})
const loading = ref(false)
const codeCountdown = ref(0)
const countdownTimer = ref(null)

const showPassword1 = ref(false)
const showPassword2 = ref(false)
const showAnyPassword = computed(() => showPassword1.value || showPassword2.value)

const mouseX = ref(0)
const mouseY = ref(0)
const isTyping = ref(false)
const isLoaded = ref(false)

onMounted(() => {
  requestAnimationFrame(() => {
    isLoaded.value = true
  })
})

function onMouseMove(e) {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}

function onInputFocus() {
  isTyping.value = true
}

function onInputBlur() {
  isTyping.value = false
}

function validate() {
  errors.email = validateEmail(form.email)
  if (!form.code) errors.code = '请输入验证码'
  else errors.code = ''
  errors.password = validatePassword(form.password)
  errors.confirmPassword = validateConfirmPassword(form.password, form.confirmPassword)
  return !errors.email && !errors.code && !errors.password && !errors.confirmPassword
}

async function sendCode() {
  errors.email = validateEmail(form.email)
  if (errors.email) {
    ElMessage.warning(errors.email)
    return
  }
  try {
    const res = await sendResetCode({email: form.email})
    if (res.success) {
      startCountdown()
      ElMessage.success('验证码已发送至邮箱，请注意查收')
    } else {
      ElMessage.error(res.errorMsg || '发送失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '发送失败，请重试')
  }
}

async function handleSubmit() {
  if (!validate()) {
    const firstError = errors.email || errors.code || errors.password || errors.confirmPassword
    ElMessage.warning(firstError)
    return
  }
  loading.value = true
  try {
    const res = await resetPassword({
      email: form.email,
      code: form.code,
      newPassword: form.password,
      confirmPassword: form.confirmPassword
    })
    if (res.success) {
      ElMessage.success('密码重置成功，请使用新密码登录')
      setTimeout(() => router.push('/login'), 1000)
    } else {
      ElMessage.error(res.errorMsg || '重置失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '网络错误')
  } finally {
    loading.value = false
  }
}

function startCountdown() {
  codeCountdown.value = 60
  countdownTimer.value = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      clearInterval(countdownTimer.value)
    }
  }, 1000)
}

onBeforeUnmount(() => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
})
</script>

<style scoped>
@keyframes slideInLeft {
  0% { transform: translateX(-100%); }
  50% { transform: translateX(5%); }
  65% { transform: translateX(0%) scaleX(0.95); }
  75% { transform: translateX(0%) scaleX(1.03); }
  85% { transform: translateX(0%) scaleX(0.99); }
  100% { transform: translateX(0%) scaleX(1); }
}

@keyframes slideInRight {
  0% { transform: translateX(100%); }
  50% { transform: translateX(-5%); }
  65% { transform: translateX(0%) scaleX(0.95); }
  75% { transform: translateX(0%) scaleX(1.03); }
  85% { transform: translateX(0%) scaleX(0.99); }
  100% { transform: translateX(0%) scaleX(1); }
}

.left-panel.slide-in-left {
  animation: slideInLeft 1.2s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

.right-panel.slide-in-right {
  animation: slideInRight 1.2s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

.login-container {
  position: fixed;
  inset: 0;
  display: flex;
  overflow: hidden;
}

.left-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(145deg, #3a4a5a 0%, #5a6a7a 50%, #4a5a6a 100%);
  transform: translateX(-100%);
}

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

.decor-grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
  linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
  z-index: 1;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
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
  animation: brandGlow 3s ease-in-out infinite;
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-name {
  color: white;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
  line-height: 1.2;
}

.brand-slogan {
  font-size: 11px;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.45);
  letter-spacing: 1px;
  line-height: 1;
}

@keyframes brandGlow {
  0%, 100% {
    box-shadow: 0 0 8px rgba(217, 138, 74, 0.25);
  }
  50% {
    box-shadow: 0 0 18px rgba(217, 138, 74, 0.55), 0 0 36px rgba(217, 138, 74, 0.15);
  }
}

.right-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  padding: 32px;
  transform: translateX(100%);
}

.auth-box {
  width: 100%;
  max-width: 400px;
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-header h1 {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 10px 0;
  letter-spacing: -0.3px;
}

.subtitle {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0;
}

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
  margin-top: 4px;
}

.submit-btn:hover:not(:disabled) {
  background: #1d4ed8;
}

.submit-btn:active:not(:disabled) {
  transform: scale(0.99);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

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

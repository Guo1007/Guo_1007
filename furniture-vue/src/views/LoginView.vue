<template>
    <div class="auth-container">
        <div class="auth-box">
            <div class="auth-header">
                <div class="logo">
                    <h1>家具商城</h1>
                </div>
                <p class="subtitle">欢迎登录</p>
            </div>

            <!-- 登录类型切换 -->
            <div class="auth-tabs">
                <button :class="['tab-btn', { active: loginType === 'code' }]" @click="loginType = 'code'">
                    验证码登录
                </button>
                <button :class="['tab-btn', { active: loginType === 'password' }]" @click="loginType = 'password'">
                    密码登录
                </button>
            </div>

            <form class="auth-form" @submit.prevent="handleLogin">
              <!-- 邮箱/手机号 -->
                <div class="form-group">
                  <label>邮箱 / 手机号</label>
                    <div class="input-wrapper">
                      <input v-model="form.account" type="text" placeholder="请输入邮箱或手机号"/>
                    </div>
                  <span class="error-msg" v-if="errors.account">{{ errors.account }}</span>
                </div>

                <!-- 验证码登录 -->
                <template v-if="loginType === 'code'">
                    <div class="form-group">
                        <label>验证码</label>
                        <div class="input-wrapper code-wrapper">
                            <input v-model="form.code" type="text" placeholder="请输入6位验证码" maxlength="6" />
                          <button type="button" class="code-btn" :disabled="codeCountdown > 0 || !form.account"
                                @click="sendVerifyCode">
                                {{ codeCountdown > 0 ? `${codeCountdown}s后重发` : '获取验证码' }}
                            </button>
                        </div>
                    </div>
                </template>

                <!-- 密码登录 -->
                <template v-else>
                    <div class="form-group">
                        <label>密码</label>
                        <div class="input-wrapper">
                            <input v-model="form.password" type="password" placeholder="请输入密码" />
                        </div>
                        <div class="form-actions">
                            <router-link to="/register" class="link-text">
                                没有账号？立即注册
                            </router-link>
                        </div>
                    </div>
                </template>

                <button type="submit" class="submit-btn" :disabled="loading">
                    <span v-if="loading">⏳</span>
                    {{ loading ? '登录中...' : '立即登录' }}
                </button>
            </form>

            <div class="auth-tips">
              <p>💡 未注册账号使用验证码登录后将自动创建账号</p>
            </div>
        </div>
    </div>
</template>

<script>
import {getUserInfo, login, sendCode} from '@/api/user.js'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {validateEmail, validatePhone} from '@/utils/validators.js'

import {useCartStore} from '@/stores/cart.js'

export default {
    name: 'LoginView',
    setup() {
        const router = useRouter()
        return { router }
    },
    data() {
        return {
            loginType: 'code',
          form: {account: '', code: '', password: ''},
          errors: {account: ''},
            loading: false,
            codeCountdown: 0,
            countdownTimer: null
        }
    },
    methods: {
      isEmail(val) {
        return val && val.includes('@')
      },
      validateAccount() {
        const val = this.form.account
        if (!val) {
          this.errors.account = '请输入邮箱或手机号'
          return false
        }
        if (this.isEmail(val)) {
          const err = validateEmail(val)
          this.errors.account = err
          return !err
        } else {
          const err = validatePhone(val)
          this.errors.account = err
          return !err
        }
        },
        async sendVerifyCode() {
          if (!this.validateAccount()) {
            ElMessage.warning(this.errors.account || '请输入正确的邮箱或手机号')
                return
            }
            try {
              const res = await sendCode({account: this.form.account})
                if (res.success) {
                    this.startCountdown()
                  const tip = this.isEmail(this.form.account) ? '验证码已发送至邮箱，请注意查收' : '验证码已发送，请注意查收'
                  ElMessage.success(tip)
                } else {
                    ElMessage.error(res.errorMsg || '发送失败')
                }
            } catch (error) {
                ElMessage.error(error.message || '发送失败，请重试')
            }
        },
        startCountdown() {
            this.codeCountdown = 60
            this.countdownTimer = setInterval(() => {
                this.codeCountdown--
                if (this.codeCountdown <= 0) {
                    clearInterval(this.countdownTimer)
                }
            }, 1000)
        },
        async handleLogin() {
          if (!this.validateAccount()) {
            ElMessage.warning(this.errors.account || '请输入正确的邮箱或手机号')
                return
            }
            if (this.loginType === 'code' && !this.form.code) {
                ElMessage.warning('请输入验证码')
                return
            }
            if (this.loginType === 'password' && !this.form.password) {
                ElMessage.warning('请输入密码')
                return
            }
            this.loading = true
            try {
                const loginData = {
                  account: this.form.account,
                    code: this.loginType === 'code' ? this.form.code : undefined,
                    passWord: this.loginType === 'password' ? this.form.password : undefined
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
                        this.router.push('/')
                    }, 800)
                } else {
                    ElMessage.error(res.errorMsg || '登录失败')
                }
            } catch (error) {
                ElMessage.error(error.message || '登录失败')
            } finally {
                this.loading = false
            }
        }
    },
    beforeUnmount() {
        if (this.countdownTimer) {
            clearInterval(this.countdownTimer)
        }
    }
}
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  padding: 20px;
}

.auth-box {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 12px;
}

.logo span {
  font-size: 32px;
}

.logo h1 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.subtitle {
  font-size: 16px;
  color: #666;
}

/* ===== 登录类型切换 ===== */
.auth-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  background: #f5f5f5;
  border-radius: 10px;
  padding: 4px;
}

.tab-btn {
  flex: 1;
  padding: 12px 16px;
  background: none;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.tab-btn.active {
  background: #fff;
  color: #333;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.tab-btn span {
  font-size: 16px;
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
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 10px;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.input-wrapper:focus-within {
  border-color: #333;
  background: #fff;
}

.input-wrapper .icon {
  padding: 0 12px;
  font-size: 18px;
}

.input-wrapper input {
  flex: 1;
  padding: 14px 12px 14px 0;
  border: none;
  background: none;
  font-size: 15px;
  color: #333;
  outline: none;
}

.input-wrapper input::placeholder {
  color: #999;
}

.code-wrapper {
  position: relative;
}

.code-wrapper input {
  padding-right: 120px;
}

.code-btn {
  position: absolute;
  right: 4px;
  padding: 8px 16px;
  background: #333;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}

.code-btn:hover:not(:disabled) {
  background: #222;
}

.code-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.error-msg {
  font-size: 12px;
  color: #e74c3c;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 4px;
}

.link-text {
  font-size: 13px;
  color: #333;
  text-decoration: none;
}

.link-text:hover {
  color: #000;
}

/* ===== 提交按钮 ===== */
.submit-btn {
  width: 100%;
  padding: 14px;
  background: #333;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.submit-btn:hover:not(:disabled) {
  background: #222;
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* ===== 提示 ===== */
.auth-tips {
  margin-top: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 10px;
  text-align: center;
}

.auth-tips p {
  font-size: 13px;
  color: #666;
  margin: 0;
}

/* ===== 响应式 ===== */
@media (max-width: 480px) {
  .auth-box {
    padding: 24px;
  }

  .logo h1 {
    font-size: 20px;
  }

  .tab-btn {
    padding: 10px 12px;
    font-size: 13px;
  }
}
</style>
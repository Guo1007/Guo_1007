<template>
    <div class="auth-container">
        <div class="auth-box">
            <div class="auth-header">
                <div class="logo">
                    <span>🏠</span>
                    <h1>家具商城</h1>
                </div>
                <p class="subtitle">欢迎登录</p>
            </div>

            <!-- 登录类型切换 -->
            <div class="auth-tabs">
                <button :class="['tab-btn', { active: loginType === 'code' }]" @click="loginType = 'code'">
                    <span>📱</span>
                    验证码登录
                </button>
                <button :class="['tab-btn', { active: loginType === 'password' }]" @click="loginType = 'password'">
                    <span>🔐</span>
                    密码登录
                </button>
            </div>

            <form class="auth-form" @submit.prevent="handleLogin">
                <!-- 手机号 -->
                <div class="form-group">
                    <label>手机号</label>
                    <div class="input-wrapper">
                        <span class="icon">📱</span>
                        <input v-model="form.phone" type="tel" placeholder="请输入11位手机号" maxlength="11" />
                    </div>
                    <span class="error-msg" v-if="errors.phone">{{ errors.phone }}</span>
                </div>

                <!-- 验证码登录 -->
                <template v-if="loginType === 'code'">
                    <div class="form-group">
                        <label>验证码</label>
                        <div class="input-wrapper code-wrapper">
                            <span class="icon">🔢</span>
                            <input v-model="form.code" type="text" placeholder="请输入6位验证码" maxlength="6" />
                            <button type="button" class="code-btn" :disabled="codeCountdown > 0 || !form.phone"
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
                            <span class="icon">🔒</span>
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
                <p>💡 未注册手机号使用短信验证码登录后将自动创建账号</p>
            </div>
        </div>
    </div>
</template>

<script>
import { sendCode, login, getUserInfo } from '@/api/user.js'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { validatePhone } from '@/utils/validators.js'
import '@/styles/views/auth.scss'
import { useCartStore } from '@/stores/cart.js'

export default {
    name: 'LoginView',
    setup() {
        const router = useRouter()
        return { router }
    },
    data() {
        return {
            loginType: 'code',
            form: { phone: '', code: '', password: '' },
            errors: { phone: '' },
            loading: false,
            codeCountdown: 0,
            countdownTimer: null
        }
    },
    methods: {
        validatePhone() {
            const error = validatePhone(this.form.phone)
            this.errors.phone = error
            return !error
        },
        async sendVerifyCode() {
            if (!this.validatePhone()) {
                ElMessage.warning('请输入正确的手机号')
                return
            }
            try {
                const res = await sendCode({ phone: this.form.phone })
                if (res.success) {
                    this.startCountdown()
                    ElMessage.success('验证码已发送，请注意查收')
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
            if (!this.validatePhone()) {
                ElMessage.warning('请输入正确的手机号')
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
                    phone: this.form.phone,
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
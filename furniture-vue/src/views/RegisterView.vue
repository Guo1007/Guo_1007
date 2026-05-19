<template>
    <div class="auth-container">
        <div class="auth-box">
            <div class="auth-header">
                <div class="logo">
                    <span>🏠</span>
                    <h1>家具商城</h1>
                </div>
                <p class="subtitle">创建新账号</p>
            </div>

            <form class="auth-form" @submit.prevent="handleRegister">
              <!-- 邮箱 -->
                <div class="form-group">
                  <label>邮箱</label>
                    <div class="input-wrapper">
                      <span class="icon">📧</span>
                      <input v-model="form.email" type="email" placeholder="请输入邮箱地址"/>
                    </div>
                  <span class="error-msg" v-if="errors.email">{{ errors.email }}</span>
                </div>

              <!-- 邮箱验证码 -->
                <div class="form-group">
                  <label>邮箱验证码</label>
                    <div class="input-wrapper code-wrapper">
                        <span class="icon">🔢</span>
                        <input v-model="form.code" type="text" placeholder="请输入6位验证码" maxlength="6" />
                      <button type="button" class="code-btn" :disabled="codeCountdown > 0 || !form.email"
                            @click="sendVerifyCode">
                            {{ codeCountdown > 0 ? `${codeCountdown}s后重发` : '获取验证码' }}
                        </button>
                    </div>
                    <span class="error-msg" v-if="errors.code">{{ errors.code }}</span>
                </div>

                <!-- 密码 -->
                <div class="form-group">
                    <label>设置密码</label>
                    <div class="input-wrapper">
                        <span class="icon">🔒</span>
                        <input v-model="form.password" type="password" placeholder="4-32位，含大小写字母和数字" />
                    </div>
                    <span class="error-msg" v-if="errors.password">{{ errors.password }}</span>
                </div>

                <!-- 确认密码 -->
                <div class="form-group">
                    <label>确认密码</label>
                    <div class="input-wrapper">
                        <span class="icon">🔐</span>
                        <input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" />
                    </div>
                    <span class="error-msg" v-if="errors.confirmPassword">{{ errors.confirmPassword }}</span>
                </div>

                <!-- 协议勾选 -->
                <div class="form-group agreement-group">
                    <label class="checkbox-label">
                        <input type="checkbox" v-model="form.agree" />
                        <span>我已阅读并同意 <a href="#" class="link-text">《用户服务协议》</a> 和 <a href="#"
                                class="link-text">《隐私政策》</a></span>
                    </label>
                    <span class="error-msg" v-if="errors.agree">{{ errors.agree }}</span>
                </div>

                <button type="submit" class="submit-btn" :disabled="loading">
                    <span v-if="loading">⏳</span>
                    {{ loading ? '注册中...' : '立即注册' }}
                </button>
            </form>

            <div class="auth-tips">
                <p>已有账号？<router-link to="/login" class="link-text">立即登录</router-link></p>
            </div>
        </div>
    </div>
</template>

<script setup>
import {onBeforeUnmount, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {register, sendRegisterCode} from '@/api/user.js'
import {validateConfirmPassword, validateEmail, validatePassword} from '@/utils/validators.js'


const router = useRouter()

const form = reactive({
  email: '',
    code: '',
    password: '',
    confirmPassword: '',
    agree: false
})

const errors = reactive({
  email: '',
    code: '',
    password: '',
    confirmPassword: '',
    agree: ''
})

const loading = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null

const sendVerifyCode = async () => {
  const error = validateEmail(form.email)
  errors.email = error
    if (error) return

    try {
      const res = await sendRegisterCode({email: form.email})
        if (res.code === 200 || res.success === true || res.code === '200') {
            startCountdown()
          ElMessage.success('验证码已发送至邮箱，请注意查收')
        } else {
            ElMessage.error(res.msg || res.errorMsg || '发送失败')
        }
    } catch (error) {
        console.error(error)
        ElMessage.error(error.message || '发送失败')
    }
}

const startCountdown = () => {
    codeCountdown.value = 60
    if (countdownTimer) clearInterval(countdownTimer)

    countdownTimer = setInterval(() => {
        codeCountdown.value--
        if (codeCountdown.value <= 0) {
            clearInterval(countdownTimer)
            countdownTimer = null
        }
    }, 1000)
}

const handleRegister = async () => {
  const isEmailValid = !validateEmail(form.email)
  errors.email = validateEmail(form.email)

    let isCodeValid = true
    if (!form.code || form.code.length !== 6) {
        errors.code = '请输入6位验证码'
        isCodeValid = false
    } else {
        errors.code = ''
    }

    const isPassValid = !validatePassword(form.password)
    errors.password = validatePassword(form.password)

    const isConfirmValid = !validateConfirmPassword(form.password, form.confirmPassword)
    errors.confirmPassword = validateConfirmPassword(form.password, form.confirmPassword)

    if (!form.agree) {
        errors.agree = '请勾选同意协议'
    } else {
        errors.agree = ''
    }

  if (!isEmailValid || !isCodeValid || !isPassValid || !isConfirmValid || !form.agree) {
        ElMessage.warning('请完善并修正表单信息')
        return
    }

    loading.value = true

    try {
        const registerData = {
          email: form.email,
            code: form.code,
            password: form.password,
            confirmPwd: form.confirmPassword
        }

        const res = await register(registerData)
        if (res.code === 200 || res.success === true || res.code === '200') {
            ElMessage.success('注册成功！即将跳转登录...')
            setTimeout(() => {
                router.push('/login')
            }, 1500)
        } else {
            ElMessage.error(res.msg || res.errorMsg || '注册失败')
        }
    } catch (error) {
        console.error('注册出错:', error)
        ElMessage.error(error.message || '注册失败，请稍后重试')
    } finally {
        loading.value = false
    }
}

onBeforeUnmount(() => {
    if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
    }
})
</script>
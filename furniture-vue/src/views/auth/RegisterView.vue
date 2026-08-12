<template>
  <!--
    RegisterView - 注册页面
    UI 完全复刻登录页面风格
    业务逻辑、表单内容完全保留
  -->
  <div class="login-container" @mousemove="onMouseMove">
    <!-- ======== 左侧卡通角色区域 ======== -->
    <div class="left-panel" :class="{ 'slide-in-left': isLoaded }">
      <!-- 网格线装饰 -->
      <div class="decor-grid"></div>
      <!-- 顶部品牌 -->
      <div class="brand">
        <div class="brand-icon">
          <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
            <rect
              width="28"
              height="28"
              rx="7"
              fill="white"
              fill-opacity="0.15"
            />
            <rect
              x="4"
              y="5"
              width="20"
              height="8"
              rx="2"
              fill="white"
              fill-opacity="0.85"
            />
            <rect
              x="3"
              y="13"
              width="22"
              height="5"
              rx="2.5"
              fill="white"
              fill-opacity="0.95"
            />
            <rect
              x="6"
              y="18"
              width="2.5"
              height="5"
              rx="1.25"
              fill="#D98A4A"
              fill-opacity="0.8"
            />
            <rect
              x="19.5"
              y="18"
              width="2.5"
              height="5"
              rx="1.25"
              fill="#D98A4A"
              fill-opacity="0.8"
            />
          </svg>
        </div>
        <div class="brand-text">
          <span class="brand-name">家具商城</span>
          <span class="brand-slogan">品质家居 · 精选好物</span>
        </div>
      </div>

      <!-- 中部卡通角色 -->
      <div
        class="flex-1 flex items-end justify-center"
        style="position: relative; z-index: 20"
      >
        <div style="width: 550px; height: 500px; position: relative">
          <AnimatedCharacters
            :mouseX="mouseX"
            :mouseY="mouseY"
            :isTyping="isTyping"
            :showPassword="showPassword"
            variant="register"
          />
        </div>
      </div>

      <!-- 底部链接 -->
      <div class="footer-links">
        <a href="javascript:void(0)">帮助中心</a>
        <a href="javascript:void(0)">隐私政策</a>
      </div>
    </div>

    <!-- ======== 右侧注册表单 ======== -->
    <div class="right-panel" :class="{ 'slide-in-right': isLoaded }">
      <div class="auth-box">
        <router-link to="/" class="back-home-link">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          返回首页
        </router-link>
        <!-- 标题 -->
        <div class="auth-header">
          <h1>创建新账号</h1>
          <p class="subtitle">加入家具商城，开启购物之旅</p>
        </div>

        <!-- 表单 -->
        <form class="auth-form" @submit.prevent="handleRegister">
          <!-- 邮箱 -->
          <div class="form-group">
            <label>邮箱</label>
            <div class="input-wrapper">
              <svg
                class="input-icon"
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path
                  d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"
                ></path>
                <polyline points="22,6 12,13 2,6"></polyline>
              </svg>
              <input
                v-model="form.email"
                type="email"
                placeholder="请输入邮箱地址"
                @focus="onInputFocus"
                @blur="onInputBlur"
              />
            </div>
            <span class="error-msg" v-if="errors.email">{{
              errors.email
            }}</span>
          </div>

          <!-- 邮箱验证码 -->
          <div class="form-group">
            <label>邮箱验证码</label>
            <div class="input-wrapper code-wrapper">
              <svg
                class="input-icon"
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <input
                v-model="form.code"
                type="text"
                placeholder="请输入6位验证码"
                maxlength="6"
                @focus="onInputFocus"
                @blur="onInputBlur"
              />
              <button
                type="button"
                class="code-btn"
                :disabled="codeCountdown > 0 || !form.email"
                @click="sendVerifyCode"
              >
                {{ codeCountdown > 0 ? `${codeCountdown}s` : "获取验证码" }}
              </button>
            </div>
            <span class="error-msg" v-if="errors.code">{{ errors.code }}</span>
          </div>

          <!-- 密码 -->
          <div class="form-group">
            <label>设置密码</label>
            <div class="input-wrapper">
              <svg
                class="input-icon"
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <input
                v-model="form.password"
                :type="showPassword1 ? 'text' : 'password'"
                placeholder="6-32位，含大小写字母和数字"
                @focus="onInputFocus"
                @blur="onInputBlur"
              />
              <button
                type="button"
                class="toggle-password"
                @click="showPassword1 = !showPassword1"
              >
                <template v-if="!showPassword1">
                  <svg
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path
                      d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"
                    ></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                </template>
                <template v-else>
                  <svg
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path
                      d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"
                    ></path>
                    <line x1="1" y1="1" x2="23" y2="23"></line>
                  </svg>
                </template>
              </button>
            </div>
            <span class="error-msg" v-if="errors.password">{{
              errors.password
            }}</span>
          </div>

          <!-- 确认密码 -->
          <div class="form-group">
            <label>确认密码</label>
            <div class="input-wrapper">
              <svg
                class="input-icon"
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <input
                v-model="form.confirmPassword"
                :type="showPassword2 ? 'text' : 'password'"
                placeholder="请再次输入密码"
                @focus="onInputFocus"
                @blur="onInputBlur"
              />
              <button
                type="button"
                class="toggle-password"
                @click="showPassword2 = !showPassword2"
              >
                <template v-if="!showPassword2">
                  <svg
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path
                      d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"
                    ></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                </template>
                <template v-else>
                  <svg
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path
                      d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"
                    ></path>
                    <line x1="1" y1="1" x2="23" y2="23"></line>
                  </svg>
                </template>
              </button>
            </div>
            <span class="error-msg" v-if="errors.confirmPassword">{{
              errors.confirmPassword
            }}</span>
          </div>

          <!-- 协议勾选 -->
          <div class="form-group agreement-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.agree" />
              <span
                >我已阅读并同意
                <a href="javascript:void(0)" class="link-text"
                  >《用户服务协议》</a
                >
                和
                <a href="javascript:void(0)" class="link-text"
                  >《隐私政策》</a
                ></span
              >
            </label>
            <span class="error-msg" v-if="errors.agree">{{
              errors.agree
            }}</span>
          </div>

          <!-- 提交按钮 -->
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? "注册中..." : "立即注册" }}
          </button>
        </form>

        <!-- 底部提示 -->
        <div class="auth-footer">
          <p>
            已有账号？
            <router-link to="/login">立即登录</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * RegisterView - 注册页面
 * UI 完全复刻登录页面风格
 * 业务逻辑、表单内容完全保留
 */
import { computed, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { register, sendRegisterCode } from "@/api/user.js";
import {
  validateConfirmPassword,
  validateEmail,
  validatePassword,
} from "@/utils/validators.js";
import { logger } from "@/utils/logger.js";
import AnimatedCharacters from "@/components/auth/AnimatedCharacters.vue";

const router = useRouter();

// ========== 表单状态 ==========
const form = reactive({
  email: "",
  code: "",
  password: "",
  confirmPassword: "",
  agree: false,
});

const errors = reactive({
  email: "",
  code: "",
  password: "",
  confirmPassword: "",
  agree: "",
});

const loading = ref(false);
const codeCountdown = ref(0);
let countdownTimer = null;

// ========== 动画状态 ==========
const mouseX = ref(0);
const mouseY = ref(0);
const isTyping = ref(false);
const isLoaded = ref(false);
const showPassword1 = ref(false);
const showPassword2 = ref(false);

// 密码是否可见（任一密码框明文时小人转身）
const showPassword = computed(() => showPassword1.value || showPassword2.value);

// ========== 载入动画 ==========
onMounted(() => {
  requestAnimationFrame(() => {
    isLoaded.value = true;
  });
});

// ========== 鼠标监听 ==========
function onMouseMove(e) {
  mouseX.value = e.clientX;
  mouseY.value = e.clientY;
}

// ========== 输入框聚焦状态 ==========
function onInputFocus() {
  isTyping.value = true;
}

function onInputBlur() {
  isTyping.value = false;
}

// ========== 发送验证码 ==========
const sendVerifyCode = async () => {
  const error = validateEmail(form.email);
  errors.email = error;
  if (error) return;

  try {
    const res = await sendRegisterCode({ email: form.email });
    if (res.code === 200 || res.success === true || res.code === "200") {
      startCountdown();
      ElMessage.success("验证码已发送至邮箱，请注意查收");
    } else {
      ElMessage.error(res.msg || "发送失败");
    }
  } catch (error) {
    logger.error("发送验证码:", error);
  }
};

// ========== 倒计时 ==========
const startCountdown = () => {
  codeCountdown.value = 60;
  if (countdownTimer) clearInterval(countdownTimer);

  countdownTimer = setInterval(() => {
    codeCountdown.value--;
    if (codeCountdown.value <= 0) {
      clearInterval(countdownTimer);
      countdownTimer = null;
    }
  }, 1000);
};

// ========== 注册处理 ==========
const handleRegister = async () => {
  const isEmailValid = !validateEmail(form.email);
  errors.email = validateEmail(form.email);

  let isCodeValid = true;
  if (!form.code || form.code.length !== 6) {
    errors.code = "请输入6位验证码";
    isCodeValid = false;
  } else {
    errors.code = "";
  }

  const isPassValid = !validatePassword(form.password);
  errors.password = validatePassword(form.password);

  const isConfirmValid = !validateConfirmPassword(
    form.password,
    form.confirmPassword,
  );
  errors.confirmPassword = validateConfirmPassword(
    form.password,
    form.confirmPassword,
  );

  if (!form.agree) {
    errors.agree = "请勾选同意协议";
  } else {
    errors.agree = "";
  }

  if (
    !isEmailValid ||
    !isCodeValid ||
    !isPassValid ||
    !isConfirmValid ||
    !form.agree
  ) {
    ElMessage.warning("请完善并修正表单信息");
    return;
  }

  loading.value = true;

  try {
    const registerData = {
      email: form.email,
      code: form.code,
      password: form.password,
      confirmPassword: form.confirmPassword,
    };

    const res = await register(registerData);
    if (res.code === 200 || res.success === true || res.code === "200") {
      ElMessage.success("注册成功！即将跳转登录...");
      setTimeout(() => {
        router.push("/login");
      }, 1500);
    } else {
      ElMessage.error(res.msg || "注册失败");
    }
  } catch (error) {
    logger.error("注册出错:", error);
  } finally {
    loading.value = false;
  }
};

// ========== 生命周期 ==========
onBeforeUnmount(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
});
</script>

<style scoped lang="scss">
@import "@/styles/views/register-view.scss";
</style>

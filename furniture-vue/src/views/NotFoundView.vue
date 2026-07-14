<template>
  <div class="not-found-page">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <div class="error-icon">🪑</div>
      <h1 class="error-title">页面不存在</h1>
      <p class="error-desc">
        很抱歉，您访问的页面可能已被移除、更名或暂时不可用。
      </p>
      <div class="error-actions">
        <el-button type="primary" size="large" @click="goHome" round>
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button size="large" @click="goBack" round>
          <el-icon><ArrowLeft /></el-icon>
          返回上页
        </el-button>
      </div>
      <div class="error-suggestion">
        <p>您可能想找：</p>
        <div class="suggestion-links">
          <a @click="router.push('/user/orders')">我的订单</a>
          <a @click="router.push('/user/favorites')">我的收藏</a>
          <a @click="router.push('/user/profile')">个人中心</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { HomeFilled, ArrowLeft } from "@element-plus/icons-vue";

const router = useRouter();

const goHome = () => router.push("/");
const goBack = () => router.back();
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e3df 100%);
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

.not-found-content {
  text-align: center;
  padding: 60px 40px;
  max-width: 520px;
}

.error-code {
  font-size: 140px;
  font-weight: 900;
  line-height: 1;
  background: linear-gradient(135deg, #d98a4a 0%, #c0392b 60%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 8px;
  margin-bottom: -20px;
}

.error-icon {
  font-size: 56px;
  margin: 16px 0;
  display: block;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-12px);
  }
}

.error-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 12px 0;
}

.error-desc {
  font-size: 15px;
  color: #999;
  line-height: 1.8;
  margin: 0 0 36px 0;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 40px;
}

.error-suggestion {
  padding-top: 28px;
  border-top: 1px solid #e0dcd8;
}

.error-suggestion p {
  font-size: 13px;
  color: #bbb;
  margin: 0 0 14px 0;
}

.suggestion-links {
  display: flex;
  gap: 24px;
  justify-content: center;
}

.suggestion-links a {
  font-size: 14px;
  color: #d98a4a;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.2s;
}

.suggestion-links a:hover {
  color: #b8733a;
  text-decoration: underline;
}
</style>

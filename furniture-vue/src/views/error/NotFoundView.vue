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

<style scoped lang="scss">
@import "@/styles/views/not-found-view.scss";
</style>

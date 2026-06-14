<template>
    <router-view />
  <AiChat v-if="showAiChat"/>
</template>

<script setup>
import {computed} from 'vue'
import {useRoute} from 'vue-router'
import AiChat from '@/components/AiChat.vue'

const route = useRoute()

// 登录、注册、管理后台页面不显示AI助手
const showAiChat = computed(() => {
  const path = route.path
  if (path === '/login' || path === '/register') return false
  if (path.startsWith('/admin')) return false
  return true
})
</script>

<style>
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    background-color: #f5f5f5;
}

#app {
    min-height: 100vh;
}
</style>
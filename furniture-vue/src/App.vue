<template>
    <router-view />
  <CartDrawer v-if="showCartDrawer"/>
  <AiChat v-if="showAiChat"/>
</template>

<script setup>
import {computed} from 'vue'
import {useRoute} from 'vue-router'
import AiChat from '@/components/AiChat.vue'
import CartDrawer from '@/components/CartDrawer.vue'

const route = useRoute()

const showAiChat = computed(() => {
  const path = route.path
  if (path === '/login' || path === '/register') return false
  if (path.startsWith('/admin')) return false
  return true
})

const showCartDrawer = computed(() => {
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
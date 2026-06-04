<template>
    <router-view />
  <AiChat/>
</template>

<script setup>
import AiChat from '@/components/AiChat.vue'
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
<template>
  <div class="default-layout">
    <AppHeader />
    <main class="layout-main">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <AppFooter />
    <CartDrawer v-if="showCart" />
    <AiChat v-if="showAi" />
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";
import AppHeader from "@/components/layout/AppHeader.vue";
import AppFooter from "@/components/layout/AppFooter.vue";
import CartDrawer from "@/components/CartDrawer.vue";
import AiChat from "@/components/AiChat.vue";

const route = useRoute();

const showCart = computed(() => {
  const p = route.path;
  return (
    p !== "/login" &&
    p !== "/register" &&
    p !== "/forgot-password" &&
    !p.startsWith("/admin")
  );
});
const showAi = computed(() => showCart.value);
</script>

<style scoped>
.default-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.layout-main {
  flex: 1;
}

/* Page transitions */
.page-enter-active,
.page-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>

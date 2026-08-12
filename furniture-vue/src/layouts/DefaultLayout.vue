<template>
  <div class="default-layout">
    <AppHeader />
    <main class="layout-main">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" :key="$route.fullPath" />
        </transition>
      </router-view>
    </main>
    <AppFooter />
    <CartDrawer v-if="showCart" />
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";
import AppHeader from "@/components/layout/AppHeader.vue";
import AppFooter from "@/components/layout/AppFooter.vue";
import CartDrawer from "@/components/cart/CartDrawer.vue";

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
</script>

<style scoped lang="scss">
@import "@/styles/views/default-layout.scss";
</style>

<template>
  <section class="service-bar">
    <!-- 骨架屏 -->
    <div v-if="loading" class="service-inner">
      <div class="svc-sk-item" v-for="i in 4" :key="i">
        <div class="svc-sk-icon"></div>
        <div class="svc-sk-text">
          <div class="svc-sk-line"></div>
          <div class="svc-sk-line short"></div>
        </div>
      </div>
    </div>

    <div v-else class="service-inner">
      <div class="service-item" v-for="svc in services" :key="svc.label">
        <span class="service-icon">{{ svc.icon }}</span>
        <div class="service-text">
          <p class="service-label">{{ svc.label }}</p>
          <p class="service-desc">{{ svc.desc }}</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useSystemStore } from "@/stores/system.js";

const sys = useSystemStore();
const services = ref([]);
const loading = ref(true);

onMounted(async () => {
  try {
    await sys.load();
    if (sys.siteData?.service) {
      services.value = sys.siteData.service.map((s) => {
        const extra = parseExtra(s.extraData);
        return {
          icon: extra.icon || "",
          label: s.contentTitle || "",
          desc: s.contentText || "",
        };
      });
    }
  } catch {
    /* ignore */
  } finally {
    loading.value = false;
  }
});

const parseExtra = (str) => {
  try {
    return JSON.parse(str) || {};
  } catch {
    return {};
  }
};
</script>

<style scoped lang="scss">
@import "@/styles/views/service-bar.scss";
</style>

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

<style scoped>
.service-bar {
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border-light);
}
.service-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-6);
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-6);
}
.service-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}
.service-icon {
  font-size: 28px;
  flex-shrink: 0;
}
.service-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}
.service-desc {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  margin-top: 2px;
}

@media (max-width: 768px) {
  .service-inner {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-3);
  }
  .service-item {
    padding: var(--space-3);
  }
}

/* Service skeleton */
@keyframes shimmer {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
.svc-sk-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
}
.svc-sk-icon {
  width: 40px;
  height: 40px;
  background: var(--color-border-light);
  border-radius: var(--radius-md);
  flex-shrink: 0;
  animation: shimmer 1.5s infinite;
}
.svc-sk-text {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}
.svc-sk-line {
  height: 14px;
  background: var(--color-border-light);
  border-radius: 4px;
  animation: shimmer 1.5s infinite;
}
.svc-sk-line.short {
  width: 60%;
}
</style>

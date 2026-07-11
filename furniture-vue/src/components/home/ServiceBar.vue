<template>
  <section class="service-bar">
    <div class="service-inner">
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
const services = [
  { icon: '🚚', label: '免费配送', desc: '全国包邮，送货上门' },
  { icon: '🔄', label: '7天无理由', desc: '不满意随时退换' },
  { icon: '🛡️', label: '质保3年', desc: '品质保障，售后无忧' },
  { icon: '🎨', label: '定制服务', desc: '个性化家具定制方案' },
]
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
.service-icon { font-size: 28px; flex-shrink: 0; }
.service-label { font-size: var(--text-sm); font-weight: 600; color: var(--color-text-primary); }
.service-desc { font-size: var(--text-xs); color: var(--color-text-tertiary); margin-top: 2px; }

@media (max-width: 768px) {
  .service-inner { grid-template-columns: repeat(2, 1fr); gap: var(--space-3); }
  .service-item { padding: var(--space-3); }
}
</style>

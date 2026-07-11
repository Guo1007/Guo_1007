<template>
  <section class="product-section">
    <div class="section-hd">
      <div>
        <h2 class="section-title">精选好物</h2>
        <p class="section-sub">用心挑选每一件家具</p>
      </div>
      <div class="section-tabs">
        <button v-for="tab in tabs" :key="tab.key"
          class="tab-btn" :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key">
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div class="product-grid" v-if="!loading && products.length > 0">
      <ProductCard v-for="item in products" :key="item.id" :product="item" />
    </div>

    <div class="loading-state" v-if="loading">
      <div class="skeleton-card" v-for="i in 4" :key="i">
        <div class="skeleton-img"></div>
        <div class="skeleton-info">
          <div class="skeleton-line short"></div>
          <div class="skeleton-line"></div>
          <div class="skeleton-line medium"></div>
        </div>
      </div>
    </div>

    <div class="section-cta">
      <router-link to="/type/0" class="view-all">
        查看全部商品
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
      </router-link>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { getFurnitureByTypeId } from '@/api/furniture.js'
import ProductCard from '@/components/product/ProductCard.vue'

const activeTab = ref('hot')
const tabs = [
  { key: 'hot', label: '本周热销' },
  { key: 'new', label: '新品首发' },
  { key: 'rec', label: '编辑推荐' },
]

const products = ref([])
const loading = ref(false)

const loadProducts = async () => {
  loading.value = true
  try {
    const params = { typeId: 0, current: 1, size: 8 }
    if (activeTab.value === 'hot') params.sortBy = 'sales'
    else if (activeTab.value === 'new') params.sortBy = 'newest'
    const res = await getFurnitureByTypeId(params)
    if ((res.success || res.code === 200) && res.data) {
      products.value = res.data.records || []
    }
  } catch { products.value = [] }
  finally { loading.value = false }
}

watch(activeTab, loadProducts)
onMounted(loadProducts)
</script>

<style scoped>
.product-section {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-12) var(--space-6);
}

.section-hd {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: var(--space-8);
  flex-wrap: wrap;
  gap: var(--space-4);
}
.section-title {
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--color-text-primary);
  font-family: var(--font-serif);
  margin-bottom: var(--space-1);
}
.section-sub {
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
}

.section-tabs {
  display: flex;
  gap: var(--space-1);
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  padding: 3px;
}
.tab-btn {
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  white-space: nowrap;
}
.tab-btn.active {
  background: var(--color-dark);
  color: #fff;
}
.tab-btn:not(.active):hover { color: var(--color-text-primary); }

/* Grid */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-5);
}

/* Skeleton */
.loading-state {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-5);
}
.skeleton-card {
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--color-surface);
}
.skeleton-img {
  aspect-ratio: 4 / 3;
  background: var(--color-border-light);
  animation: shimmer 1.5s infinite;
}
.skeleton-info { padding: var(--space-4); display: flex; flex-direction: column; gap: var(--space-2); }
.skeleton-line {
  height: 14px;
  background: var(--color-border-light);
  border-radius: 4px;
  animation: shimmer 1.5s infinite;
}
.skeleton-line.short { width: 60%; }
.skeleton-line.medium { width: 40%; }

@keyframes shimmer {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* CTA */
.section-cta { text-align: center; margin-top: var(--space-8); }
.view-all {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-6);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: all var(--transition-fast);
}
.view-all:hover { border-color: var(--color-dark); color: var(--color-text-primary); }

@media (max-width: 1024px) {
  .product-grid, .loading-state { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .product-grid, .loading-state { grid-template-columns: repeat(2, 1fr); gap: var(--space-3); }
  .section-hd { flex-direction: column; align-items: flex-start; }
}
</style>

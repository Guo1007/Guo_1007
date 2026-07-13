<template>
  <div class="home-page">
    <!-- Hero Carousel -->
    <HeroBanner />

    <!-- Service Bar -->
    <ServiceBar />

    <!-- Categories -->
    <section class="categories-section">
      <div class="section-hd">
        <div>
          <h2 class="section-title">家具分类</h2>
          <p class="section-sub">选择你感兴趣的品类</p>
        </div>
      </div>

      <div v-if="catLoading" class="cat-skeleton">
        <div class="cat-sk-item" v-for="i in 6" :key="i">
          <div class="cat-sk-icon"></div>
          <div class="cat-sk-line"></div>
        </div>
      </div>

      <div class="cat-grid" v-else-if="categories.length > 0">
        <router-link v-for="cat in categories" :key="cat.id"
          :to="`/type/${cat.id}`" class="cat-card"
          @click="saveTypeInfo(cat)">
          <div class="cat-icon-box">
            <span v-if="cat.icon && !isImgUrl(cat.icon)" class="cat-emoji">{{ cat.icon }}</span>
            <img v-else-if="cat.icon" :src="imgUrl(cat.icon)" alt="" class="cat-icon-img" />
            <span v-else class="cat-emoji">🪑</span>
          </div>
          <h3 class="cat-name">{{ cat.name }}</h3>
          <p class="cat-desc">{{ cat.title || cat.name + '系列家具' }}</p>
        </router-link>
      </div>

      <div v-else class="empty-state">
        <p>暂无分类数据</p>
      </div>
    </section>

    <!-- Product Tabs -->
    <ProductTabs />

    <!-- Brand Story -->
    <section class="brand-section">
      <div class="brand-inner">
        <div class="brand-text">
          <p class="brand-label">关于我们</p>
          <h2 class="brand-title">用心打造每一件家具</h2>
          <p class="brand-desc">
            WOODSPACE 创立于 2018 年，专注于将自然材质与现代设计完美融合。
            我们相信，好的家具不仅是功能性的存在，更是承载生活记忆与情感的空间伴侣。
            每一件作品背后，都凝聚着匠人对细节的执着与对美的追求。
          </p>
          <router-link to="/about" class="brand-link">
            了解更多关于我们的故事
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
          </router-link>
        </div>
        <div class="brand-visual">
          <div class="brand-img-placeholder">
            <span>🪵</span>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getFurnitureTypeList } from '@/api/furniture.js'
import { imgUrl } from '@/utils/img.js'
import HeroBanner from '@/components/home/HeroBanner.vue'
import ServiceBar from '@/components/home/ServiceBar.vue'
import ProductTabs from '@/components/home/ProductTabs.vue'

const categories = ref([])
const catLoading = ref(true)
const isImgUrl = (str) => str && (str.startsWith('/') || str.startsWith('http'))

const saveTypeInfo = (cat) => {
  sessionStorage.setItem('currentType', JSON.stringify({
    id: cat.id, name: cat.name, icon: cat.icon, title: cat.title
  }))
}

const loadCategories = async () => {
  catLoading.value = true
  try {
    const res = await getFurnitureTypeList()
    if (res.success && Array.isArray(res.data)) categories.value = res.data
  } catch { categories.value = [] }
  finally { catLoading.value = false }
}

onMounted(loadCategories)
</script>

<style scoped>
.home-page { background: var(--color-bg); }

/* Section headers */
.section-hd {
  text-align: center;
  margin-bottom: var(--space-8);
}
.section-title {
  font-size: var(--text-2xl);
  font-weight: 700;
  font-family: var(--font-serif);
  color: var(--color-text-primary);
  margin-bottom: var(--space-1);
}
.section-sub {
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
}

/* Categories */
.categories-section {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-12) var(--space-6);
}
.cat-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: var(--space-4);
}
.cat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-6) var(--space-4);
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
  text-decoration: none;
  transition: all var(--transition-normal);
}
.cat-card:hover {
  border-color: var(--color-border);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.cat-icon-box {
  width: 56px;
  height: 56px;
  background: var(--color-bg);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-3);
}
.cat-emoji { font-size: 28px; }
.cat-icon-img { width: 32px; height: 32px; object-fit: contain; }
.cat-name { font-size: var(--text-sm); font-weight: 600; color: var(--color-text-primary); margin-bottom: var(--space-1); }
.cat-desc { font-size: var(--text-xs); color: var(--color-text-tertiary); text-align: center; }

/* Cat skeleton */
.cat-skeleton {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: var(--space-4);
}
.cat-sk-item {
  padding: var(--space-8) var(--space-4);
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
}
.cat-sk-icon {
  width: 56px;
  height: 56px;
  background: var(--color-border-light);
  border-radius: var(--radius-md);
  animation: shimmer 1.5s infinite;
}
.cat-sk-line {
  width: 60%;
  height: 14px;
  background: var(--color-border-light);
  border-radius: 4px;
  animation: shimmer 1.5s infinite;
}
@keyframes shimmer {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.empty-state { text-align: center; padding: var(--space-10) 0; color: var(--color-text-tertiary); }

/* Brand Story */
.brand-section {
  background: var(--color-dark);
  color: #fff;
}
.brand-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-16) var(--space-6);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-12);
  align-items: center;
}
.brand-label {
  font-size: var(--text-xs);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: var(--color-accent);
  margin-bottom: var(--space-4);
}
.brand-title {
  font-size: var(--text-3xl);
  font-weight: 700;
  font-family: var(--font-serif);
  margin-bottom: var(--space-6);
  color: #fff;
}
.brand-desc {
  font-size: var(--text-sm);
  color: rgba(255,255,255,0.6);
  line-height: var(--leading-relaxed);
  margin-bottom: var(--space-8);
}
.brand-link {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: #fff;
  font-size: var(--text-sm);
  font-weight: 500;
  text-decoration: none;
  border-bottom: 1px solid rgba(255,255,255,0.3);
  padding-bottom: 2px;
  transition: border-color var(--transition-fast);
}
.brand-link:hover { border-color: #fff; }
.brand-visual {
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-img-placeholder {
  width: 320px;
  height: 320px;
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80px;
}

@media (max-width: 1024px) {
  .cat-grid, .cat-skeleton { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 768px) {
  .cat-grid, .cat-skeleton { grid-template-columns: repeat(3, 1fr); }
  .brand-inner { grid-template-columns: 1fr; gap: var(--space-8); padding: var(--space-10) var(--space-4); }
  .brand-visual { display: none; }
  .brand-title { font-size: var(--text-2xl); }
}
@media (max-width: 480px) {
  .cat-grid, .cat-skeleton { grid-template-columns: repeat(3, 1fr); gap: var(--space-2); }
  .cat-card { padding: var(--space-4) var(--space-2); }
}
</style>

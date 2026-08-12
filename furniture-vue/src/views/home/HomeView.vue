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
          <h2 class="section-title">{{ catTitle }}</h2>
          <p class="section-sub">{{ catSub }}</p>
        </div>
      </div>

      <div v-if="catLoading" class="cat-skeleton">
        <div class="cat-sk-item" v-for="i in 6" :key="i">
          <div class="cat-sk-icon"></div>
          <div class="cat-sk-line"></div>
        </div>
      </div>

      <div class="cat-grid" v-else-if="categories.length > 0">
        <router-link
          v-for="cat in categories"
          :key="cat.id"
          :to="`/type/${cat.id}`"
          class="cat-card"
          @click="saveTypeInfo(cat)"
        >
          <div class="cat-icon-box">
            <span v-if="cat.icon && !isImgUrl(cat.icon)" class="cat-emoji">{{
              cat.icon
            }}</span>
            <img
              v-else-if="cat.icon"
              :src="imgUrl(cat.icon)"
              alt=""
              class="cat-icon-img"
            />
            <span v-else class="cat-emoji">🪑</span>
          </div>
          <h3 class="cat-name">{{ cat.name }}</h3>
          <p class="cat-desc">{{ cat.title || cat.name + "系列家具" }}</p>
        </router-link>
      </div>

      <div v-else class="empty-state">
        <p>暂无分类数据</p>
      </div>
    </section>

    <!-- Product Tabs -->
    <ProductTabs />

    <!-- Scene Guide Section -->
    <section class="scene-section">
      <div class="scene-inner">
        <div class="scene-hd">
          <p class="scene-label">按场景选购</p>
          <h2 class="scene-title">按场景选家具 · 省心又好看</h2>
          <p class="scene-sub">精选 3 大生活场景套装，一键直达对应分类，搭配更划算</p>
        </div>
        <div class="scene-grid">
          <router-link
            v-for="s in sceneList"
            :key="s.key"
            :to="`/type/${s.typeId}`"
            class="scene-card"
            @click="saveTypeInfo(s.category)"
          >
            <div class="scene-icon">{{ s.icon }}</div>
            <h3 class="scene-card-title">{{ s.label }}</h3>
            <p class="scene-card-desc">{{ s.desc }}</p>
            <div class="scene-card-tag">{{ s.highlight }}</div>
            <div class="scene-card-cta">
              去看看
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
            </div>
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { getFurnitureTypeList } from "@/api/furniture.js";
import { imgUrl } from "@/utils/img.js";
import { useSystemStore } from "@/stores/system.js";
import HeroBanner from "@/components/home/HeroBanner.vue";
import ServiceBar from "@/components/home/ServiceBar.vue";
import ProductTabs from "@/components/home/ProductTabs.vue";

const sys = useSystemStore();
const categories = ref([]);
const catLoading = ref(true);
const catTitle = ref("家具分类");
const catSub = ref("选择你感兴趣的品类");

const isImgUrl = (str) =>
  str && (str.startsWith("/") || str.startsWith("http"));

const saveTypeInfo = (cat) => {
  if (!cat) return;
  sessionStorage.setItem(
    "currentType",
    JSON.stringify({
      id: cat.id,
      name: cat.name,
      icon: cat.icon,
      title: cat.title,
    }),
  );
};

// 场景导购预设（通过 matchTypeName 与分类接口返回做匹配）
const SCENE_PRESETS = [
  {
    key: "living",
    label: "客厅焕新",
    icon: "🏠",
    desc: "沙发 · 茶几 · 鞋柜一站配齐",
    matchTypeName: "门厅系列",
    highlight: "热销组合",
  },
  {
    key: "bedroom",
    label: "卧室套装",
    icon: "🛏️",
    desc: "床垫 · 衣柜 · 床头柜",
    matchTypeName: "卧室系列",
    highlight: "焕新睡眠",
  },
  {
    key: "study",
    label: "书房配齐",
    icon: "📚",
    desc: "书桌 · 书架组合",
    matchTypeName: "书房系列",
    highlight: "高效办公",
  },
];

const sceneList = computed(() => {
  return SCENE_PRESETS.map((preset) => {
    const cat = categories.value.find((c) => c.name === preset.matchTypeName);
    return { ...preset, typeId: cat ? cat.id : 0, category: cat };
  }).filter((s) => s.typeId > 0);
});

const loadSiteLabels = async () => {
  try {
    await sys.load();
    const labels = sys.siteData?.label || [];
    const cat = labels.find((l) => l.sectionKey === "home_categories");
    if (cat) {
      catTitle.value = cat.contentTitle;
      if (cat.contentText) catSub.value = cat.contentText;
    }
  } catch {
    /* ignore */
  }
};

const loadCategories = async () => {
  catLoading.value = true;
  try {
    const res = await getFurnitureTypeList();
    if (res.success && Array.isArray(res.data)) categories.value = res.data;
  } catch {
    categories.value = [];
  } finally {
    catLoading.value = false;
  }
};

onMounted(async () => {
  loadCategories();
  loadSiteLabels();
});
</script>

<style scoped>
.home-page {
  background: var(--color-bg);
}

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
.cat-emoji {
  font-size: 28px;
}
.cat-icon-img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}
.cat-name {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--space-1);
}
.cat-desc {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  text-align: center;
}

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
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.empty-state {
  text-align: center;
  padding: var(--space-10) 0;
  color: var(--color-text-tertiary);
}

/* Scene Guide */
.scene-section {
  background: var(--color-bg);
}
.scene-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-12) var(--space-6);
}
.scene-hd {
  text-align: center;
  margin-bottom: var(--space-10);
}
.scene-label {
  font-size: var(--text-xs);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: var(--color-accent);
  margin-bottom: var(--space-4);
}
.scene-title {
  font-size: var(--text-2xl);
  font-weight: 700;
  font-family: var(--font-serif);
  color: var(--color-text-primary);
  margin-bottom: var(--space-2);
}
.scene-sub {
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
}
.scene-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-6);
}
.scene-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: var(--space-8) var(--space-6);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  text-decoration: none;
  transition: all var(--transition-normal);
}
.scene-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-border);
}
.scene-icon {
  font-size: 48px;
  margin-bottom: var(--space-4);
}
.scene-card-title {
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--space-2);
}
.scene-card-desc {
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
  margin-bottom: var(--space-3);
}
.scene-card-tag {
  display: inline-block;
  font-size: var(--text-xs);
  color: var(--color-accent);
  background: rgba(194, 154, 110, 0.1);
  padding: 2px var(--space-2);
  border-radius: var(--radius-sm);
  margin-bottom: var(--space-4);
}
.scene-card-cta {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-text-secondary);
  margin-top: auto;
}

@media (max-width: 1024px) {
  .cat-grid,
  .cat-skeleton {
    grid-template-columns: repeat(4, 1fr);
  }
}
@media (max-width: 768px) {
  .cat-grid,
  .cat-skeleton {
    grid-template-columns: repeat(3, 1fr);
  }
  .scene-grid {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 480px) {
  .cat-grid,
  .cat-skeleton {
    grid-template-columns: repeat(3, 1fr);
    gap: var(--space-2);
  }
  .cat-card {
    padding: var(--space-4) var(--space-2);
  }
}
</style>

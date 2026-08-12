<template>
  <section class="product-section">
    <div class="section-hd">
      <div>
        <h2 class="section-title">{{ sectionTitle }}</h2>
        <p class="section-sub">{{ sectionSub }}</p>
      </div>
      <div class="section-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-btn"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div class="product-grid" v-if="!loading && products.length > 0">
      <ProductCard
        v-for="item in products"
        :key="item.id"
        :product="item"
        :badge="activeTab"
      />
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
        <svg
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
        >
          <path d="M5 12h14M12 5l7 7-7 7" />
        </svg>
      </router-link>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, watch } from "vue";
import { getFurnitureByTypeId } from "@/api/furniture.js";
import { useSystemStore } from "@/stores/system.js";
import ProductCard from "@/components/product/ProductCard.vue";

const sys = useSystemStore();
const sectionTitle = ref("精选好物");
const sectionSub = ref("用心挑选每一件家具");
const activeTab = ref("hot");
const tabs = [
  { key: "hot", label: "热销排行" },
  { key: "new", label: "新品首发" },
  { key: "rec", label: "管理推荐" },
];

const products = ref([]);
const loading = ref(false);

const loadProducts = async () => {
  loading.value = true;
  try {
    const params = { typeId: 0, current: 1, size: 8 };
    if (activeTab.value === "hot") params.sortBy = "sales";
    else if (activeTab.value === "new") params.sortBy = "newest";
    else if (activeTab.value === "rec") {
      params.isRecommended = 1;
    }
    const res = await getFurnitureByTypeId(params);
    if ((res.success || res.code === 200) && res.data) {
      products.value = res.data.records || [];
    }
  } catch {
    products.value = [];
  } finally {
    loading.value = false;
  }
};

const loadLabels = async () => {
  try {
    await sys.load();
    const labels = sys.siteData?.label || [];
    const p = labels.find((l) => l.sectionKey === "home_products");
    if (p) {
      sectionTitle.value = p.contentTitle;
      sectionSub.value = p.contentText || sectionSub.value;
    }
  } catch {
    /* ignore */
  }
};

watch(activeTab, loadProducts);
onMounted(() => {
  loadLabels();
  loadProducts();
});
</script>

<style scoped lang="scss">
@import "@/styles/views/product-tabs.scss";
</style>

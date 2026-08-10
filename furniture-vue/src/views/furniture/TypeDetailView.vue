<template>
  <div class="category-page">
    <div class="category-container">
      <!-- Breadcrumb -->
      <div class="breadcrumb">
        <button class="breadcrumb-back" @click="goBack" title="返回">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        </button>
        <router-link to="/">首页</router-link>
        <span>/</span>
        <span class="current">{{ typeInfo.name || "全部商品" }}</span>
      </div>

      <!-- Category Header -->
      <div class="cat-banner" v-if="typeInfo.name">
        <div>
          <h1 class="cat-title">{{ typeInfo.name }}</h1>
          <p class="cat-desc">
            {{
              typeInfo.title ||
              "探索" + typeInfo.name + "系列的极致工艺与舒适体验"
            }}
          </p>
        </div>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="toolbar-left">
          <span class="total-count">共 {{ total }} 件商品</span>
        </div>
        <div class="toolbar-right">
          <div class="sort-group">
            <button
              v-for="s in sortOptions"
              :key="s.value"
              class="sort-btn"
              :class="{ active: sortBy === s.value }"
              @click="setSort(s.value)"
            >
              {{ s.label }}
            </button>
          </div>
          <div class="view-toggle">
            <button
              class="view-btn"
              :class="{ active: viewMode === 'grid' }"
              @click="viewMode = 'grid'"
              title="网格视图"
            >
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="currentColor"
              >
                <rect x="3" y="3" width="7" height="7" rx="1" />
                <rect x="14" y="3" width="7" height="7" rx="1" />
                <rect x="3" y="14" width="7" height="7" rx="1" />
                <rect x="14" y="14" width="7" height="7" rx="1" />
              </svg>
            </button>
            <button
              class="view-btn"
              :class="{ active: viewMode === 'list' }"
              @click="viewMode = 'list'"
              title="列表视图"
            >
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="currentColor"
              >
                <rect x="3" y="4" width="18" height="3" rx="1" />
                <rect x="3" y="10" width="18" height="3" rx="1" />
                <rect x="3" y="16" width="18" height="3" rx="1" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <div class="cat-layout" :class="{ 'with-sidebar': showSidebar }">
        <!-- Sidebar filters (desktop) -->
        <aside class="filter-sidebar" v-if="showSidebar">
          <div class="filter-block">
            <h4 class="filter-title">搜索</h4>
            <el-input
              v-model="searchForm.fName"
              placeholder="输入名称关键词..."
              clearable
              size="default"
              @keyup.enter="handleSearch"
              @clear="searchForm.fName = ''"
            />
          </div>

          <div class="filter-block">
            <h4 class="filter-title">品牌</h4>
            <div class="filter-scroll">
              <label
                v-for="brand in brandOptions"
                :key="brand.value"
                class="filter-checkbox"
              >
                <input
                  type="radio"
                  :value="brand.value"
                  v-model="searchForm.brand"
                  @change="handleSearch"
                  name="brand"
                />
                <span class="filter-checkmark"></span>
                <span class="filter-label">{{ brand.label }}</span>
              </label>
            </div>
            <button
              v-if="searchForm.brand"
              class="filter-clear"
              @click="
                searchForm.brand = undefined;
                handleSearch();
              "
            >
              清除品牌筛选
            </button>
          </div>

          <div class="filter-block">
            <h4 class="filter-title">库存状态</h4>
            <label class="filter-checkbox">
              <input
                type="radio"
                value=""
                v-model="searchForm.stockStatus"
                @change="handleSearch"
                name="stock"
              />
              <span class="filter-checkmark"></span>
              <span class="filter-label">全部</span>
            </label>
            <label class="filter-checkbox">
              <input
                type="radio"
                value="in_stock"
                v-model="searchForm.stockStatus"
                @change="handleSearch"
                name="stock"
              />
              <span class="filter-checkmark"></span>
              <span class="filter-label">有库存</span>
            </label>
            <label class="filter-checkbox">
              <input
                type="radio"
                value="low_stock"
                v-model="searchForm.stockStatus"
                @change="handleSearch"
                name="stock"
              />
              <span class="filter-checkmark"></span>
              <span class="filter-label">库存紧张</span>
            </label>
          </div>

          <button class="filter-reset" @click="resetSearch">
            重置所有筛选
          </button>
        </aside>

        <!-- Main content -->
        <div class="cat-main">
          <!-- Loading -->
          <div v-if="loading" class="loading-state">
            <div class="skeleton-grid" :class="viewMode">
              <div class="skeleton-card" v-for="i in 8" :key="i">
                <div class="sk-img"></div>
                <div class="sk-info">
                  <div class="sk-line short"></div>
                  <div class="sk-line"></div>
                  <div class="sk-line medium"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- Empty -->
          <div v-else-if="furnitureList.length === 0" class="empty-state">
            <span class="empty-icon">📦</span>
            <p>该分类暂无家具</p>
            <p class="empty-tip" v-if="hasActiveFilters">
              当前筛选条件无匹配结果
            </p>
            <button
              class="clear-filters-btn"
              v-if="hasActiveFilters"
              @click="resetSearchAndLoad"
            >
              清除筛选条件
            </button>
            <router-link to="/" class="back-home-btn" v-else
              >去其他分类看看</router-link
            >
          </div>

          <!-- Grid view -->
          <div v-else-if="viewMode === 'grid'" class="product-grid">
            <ProductCard
              v-for="item in furnitureList"
              :key="item.id"
              :product="item"
            />
          </div>

          <!-- List view -->
          <div v-else class="product-list">
            <div
              class="list-item"
              v-for="item in furnitureList"
              :key="item.id"
              @click="goToDetail(item)"
            >
              <div class="list-img-wrap">
                <img
                  :src="imgUrl(item.fIcon)"
                  :alt="item.fName"
                  class="list-img"
                  @error="handleImgError"
                />
                <div class="list-badges">
                  <span
                    class="badge badge-low"
                    v-if="item.stock > 0 && item.stock < 10"
                    >库存紧张</span
                  >
                  <span class="badge badge-out" v-if="item.stock === 0"
                    >暂时缺货</span
                  >
                </div>
              </div>
              <div class="list-info">
                <p class="list-brand" v-if="item.brand">{{ item.brand }}</p>
                <h3 class="list-name">{{ item.fName }}</h3>
                <p class="list-intro" v-if="item.intro">{{ item.intro }}</p>
                <div class="list-footer">
                  <span class="list-price">¥{{ formatPrice(item.price) }}</span>
                  <button
                    class="list-cart-btn"
                    @click.stop="quickAdd(item)"
                    :disabled="item.stock === 0"
                  >
                    加入购物车
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Pagination -->
          <div class="pagination-wrapper" v-if="total > 0 && !loading">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[12, 24, 48]"
              layout="total, sizes, prev, pager, next"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>
    </div>

    <SpecSelectDialog
      v-model:visible="specDialogVisible"
      :product="specTarget || {}"
      :spec-groups="specData.specGroups"
      :sku-list="specData.skuList"
      @confirm="onSpecConfirm"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { getFurnitureBrands, getFurnitureByTypeId, getFurnitureSpecs } from "@/api/furniture.js";
import { imgUrl } from "@/utils/img.js";
import { formatPrice } from "@/utils/format.js";
import { logger } from "@/utils/logger.js";
import { useCartStore } from "@/stores/cart.js";
import { useBackNavigation } from '@/composables/useBackNavigation.js';
import ProductCard from "@/components/product/ProductCard.vue";
import SpecSelectDialog from "@/components/product/SpecSelectDialog.vue";

const cartStore = useCartStore();
const route = useRoute();
const router = useRouter();
const { goBack } = useBackNavigation();
const typeId = ref(route.params.id);
const isAllCategories = computed(() => typeId.value === "0");

const viewMode = ref("grid");
const sortBy = ref("default");
const sortOptions = [
  { label: "综合", value: "default" },
  { label: "销量优先", value: "sales" },
  { label: "价格从低到高", value: "price_asc" },
  { label: "价格从高到低", value: "price_desc" },
  { label: "最新上架", value: "newest" },
];

const searchForm = ref({
  fName: route.query.keyword || "",
  stockStatus: "",
  brand: undefined,
});

const brandOptions = ref([]);
const furnitureList = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(12);
const total = ref(0);
const typeInfo = ref({});

const showSidebar = ref(true);

const hasActiveFilters = computed(() => {
  return !!(
    searchForm.value.fName?.trim() ||
    searchForm.value.stockStatus ||
    searchForm.value.brand
  );
});

const activeFilterCount = computed(() => {
  let c = 0;
  if (searchForm.value.fName?.trim()) c++;
  if (searchForm.value.stockStatus) c++;
  if (searchForm.value.brand) c++;
  return c;
});

const specDialogVisible = ref(false);
const specData = ref({ specGroups: [], skuList: [] });
const specTarget = ref(null);

const setSort = (val) => {
  sortBy.value = val;
  currentPage.value = 1;
  loadFurnitureList();
};

const loadBrands = async () => {
  try {
    const res = await getFurnitureBrands(typeId.value);
    if (res.success || res.code === 200) {
      brandOptions.value = (res.data || [])
        .map((name) => ({ value: name, label: name }))
        .sort((a, b) => a.label.localeCompare(b.label, "zh-CN"));
    }
  } catch {
    brandOptions.value = [];
  }
};

const loadFurnitureList = async () => {
  loading.value = true;
  try {
    const params = { current: currentPage.value, size: pageSize.value };
    if (isAllCategories.value) {
      params.typeId = 0;
      if (searchForm.value.fName?.trim())
        params.keyword = searchForm.value.fName.trim();
    } else {
      params.typeId = typeId.value;
      if (searchForm.value.fName?.trim())
        params.fName = searchForm.value.fName.trim();
    }
    if (searchForm.value.stockStatus)
      params.stockStatus = searchForm.value.stockStatus;
    if (searchForm.value.brand) params.brand = searchForm.value.brand;
    if (sortBy.value === "sales") params.sortBy = "sales";
    else if (sortBy.value === "newest") params.sortBy = "newest";
    else if (sortBy.value === "price_asc") {
      params.sortBy = "price";
      params.sortOrder = "asc";
    } else if (sortBy.value === "price_desc") {
      params.sortBy = "price";
      params.sortOrder = "desc";
    }

    const res = await getFurnitureByTypeId(params);
    if ((res.success || res.code === 200) && res.data) {
      furnitureList.value = res.data.records || [];
      total.value = res.data.total || 0;
      if (furnitureList.value.length === 0 && currentPage.value > 1) {
        currentPage.value = 1;
        await loadFurnitureList();
      }
    } else {
      furnitureList.value = [];
      total.value = 0;
    }
  } catch (e) {
    logger.error("加载家具列表失败:", e);
    furnitureList.value = [];
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  loadFurnitureList();
};
const resetSearch = () => {
  searchForm.value = { fName: "", stockStatus: "", brand: undefined };
  handleSearch();
};
const resetSearchAndLoad = () => resetSearch();
const handleSizeChange = (v) => {
  pageSize.value = v;
  currentPage.value = 1;
  loadFurnitureList();
};
const handleCurrentChange = (v) => {
  currentPage.value = v;
  loadFurnitureList();
};

const loadTypeInfo = () => {
  if (isAllCategories.value) {
    typeInfo.value = { name: "全部商品", title: "搜索浏览所有家具" };
    return;
  }
  const cached = sessionStorage.getItem("currentType");
  if (cached) {
    const parsed = JSON.parse(cached);
    if (parsed.id == typeId.value) typeInfo.value = parsed;
  }
  if (!typeInfo.value.name) typeInfo.value = { name: "家具系列" };
};

const goToDetail = (item) =>
  router.push({ name: "FurnitureDetail", params: { id: item.id } });
const quickAdd = async (item) => {
  if (item.stock === 0) {
    ElMessage.warning("该商品已缺货");
    return;
  }
  try {
    const res = await getFurnitureSpecs(item.id);
    const groups = res.data?.specGroups || [];
    const skus = res.data?.skuList || [];

    if (groups.length > 0) {
      specTarget.value = item;
      specData.value = { specGroups: groups, skuList: skus };
      specDialogVisible.value = true;
    } else {
      const skuInfo = skus.length === 1
        ? {
            skuId: skus[0].id,
            price: skus[0].price,
            stock: skus[0].stock,
            skuImage: skus[0].skuImage,
            specText: skus[0].specText || '',
          }
        : null;
      cartStore.addItem(item, 1, skuInfo);
    }
  } catch {
    cartStore.addItem(item, 1);
  }
};

const onSpecConfirm = (skuInfo) => {
  if (specTarget.value) {
    cartStore.addItem(specTarget.value, 1, skuInfo);
    specTarget.value = null;
  }
  specDialogVisible.value = false;
};
const handleImgError = (e) => {
  e.target.style.display = "none";
};

watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      typeId.value = newId;
      const keyword = route.query.keyword || "";
      searchForm.value = { fName: keyword, stockStatus: "", brand: undefined };
      sortBy.value = "default";
      currentPage.value = 1;
      loadTypeInfo();
      if (!isAllCategories.value) loadBrands();
      loadFurnitureList();
    }
  },
);

onMounted(() => {
  loadTypeInfo();
  if (!isAllCategories.value) loadBrands();
  if (route.query.keyword) searchForm.value.fName = route.query.keyword;
  loadFurnitureList();
});
</script>

<style scoped>
.category-page {
  background: var(--color-bg);
  min-height: 60vh;
}
.category-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-6);
}

/* Breadcrumb */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  margin-bottom: var(--space-4);
}
.breadcrumb a {
  color: var(--color-text-tertiary);
  text-decoration: none;
}
.breadcrumb a:hover {
  color: var(--color-text-primary);
}
.current {
  color: var(--color-text-primary);
}

.breadcrumb-back {
  display: flex; align-items: center; justify-content: center;
  width: 26px; height: 26px; border-radius: 50%;
  border: none; background: transparent;
  color: var(--color-text-tertiary);
  transition: all var(--transition-fast);
  margin-right: var(--space-2); flex-shrink: 0; cursor: pointer;
}
.breadcrumb-back:hover {
  background: var(--color-border-light);
  color: var(--color-text-primary);
}

/* Banner */
.cat-banner {
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: var(--space-8);
  margin-bottom: var(--space-5);
}
.cat-title {
  font-size: var(--text-3xl);
  font-weight: 700;
  font-family: var(--font-serif);
  color: var(--color-text-primary);
  margin-bottom: var(--space-2);
}
.cat-desc {
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
}

/* Toolbar */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-5);
  padding: var(--space-3) 0;
  flex-wrap: wrap;
  gap: var(--space-3);
}
.total-count {
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.sort-group {
  display: flex;
  gap: var(--space-1);
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  padding: 3px;
}
.sort-btn {
  padding: var(--space-1) var(--space-3);
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  border-radius: var(--radius-sm);
  white-space: nowrap;
  transition: all var(--transition-fast);
}
.sort-btn.active {
  background: var(--color-dark);
  color: #fff;
}
.sort-btn:not(.active):hover {
  color: var(--color-text-primary);
}

.view-toggle {
  display: flex;
  gap: 2px;
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  padding: 2px;
}
.view-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  color: var(--color-text-tertiary);
  transition: all var(--transition-fast);
}
.view-btn.active {
  background: var(--color-bg);
  color: var(--color-text-primary);
}

/* Layout */
.cat-layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--space-6);
}
.cat-layout.with-sidebar {
  grid-template-columns: 220px 1fr;
}

/* Sidebar */
.filter-sidebar {
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  height: fit-content;
  position: sticky;
  top: calc(var(--header-height) + var(--space-6));
}
.filter-block {
  margin-bottom: var(--space-5);
  padding-bottom: var(--space-5);
  border-bottom: 1px solid var(--color-border-light);
}
.filter-block:last-of-type {
  border-bottom: none;
  margin-bottom: var(--space-4);
  padding-bottom: 0;
}
.filter-title {
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: var(--space-3);
}

.filter-checkbox {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) 0;
  cursor: pointer;
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  user-select: none;
}
.filter-checkbox:hover {
  color: var(--color-text-primary);
}
.filter-checkbox input {
  display: none;
}
.filter-checkmark {
  width: 16px;
  height: 16px;
  border: 2px solid var(--color-border);
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.filter-checkbox input:checked + .filter-checkmark {
  border-color: var(--color-dark);
  background: var(--color-dark);
}
.filter-checkbox input:checked + .filter-checkmark::after {
  content: "";
  width: 6px;
  height: 6px;
  background: #fff;
  border-radius: 50%;
}
.filter-scroll {
  max-height: 200px;
  overflow-y: auto;
}
.filter-clear {
  margin-top: var(--space-2);
  font-size: var(--text-xs);
  color: var(--color-accent);
  cursor: pointer;
}
.filter-reset {
  width: 100%;
  padding: var(--space-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
  background: transparent;
}
.filter-reset:hover {
  border-color: var(--color-dark);
  color: var(--color-text-primary);
}

/* Product Grid */
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
}

/* Product List */
.product-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.list-item {
  display: flex;
  gap: var(--space-5);
  padding: var(--space-4);
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-normal);
}
.list-item:hover {
  border-color: var(--color-border);
  box-shadow: var(--shadow-sm);
}
.list-img-wrap {
  position: relative;
  width: 180px;
  height: 140px;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--color-bg);
}
.list-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.list-badges {
  position: absolute;
  top: var(--space-2);
  left: var(--space-2);
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.badge {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: var(--radius-sm);
}
.badge-low {
  background: var(--color-warning);
  color: #fff;
}
.badge-out {
  background: var(--color-text-tertiary);
  color: #fff;
}

.list-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.list-brand {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  margin-bottom: var(--space-1);
}
.list-name {
  font-size: var(--text-md);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--space-1);
}
.list-intro {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  margin-bottom: var(--space-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}
.list-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}
.list-price {
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-accent);
}
.list-cart-btn {
  padding: var(--space-2) var(--space-5);
  background: var(--color-dark);
  color: #fff;
  font-size: var(--text-xs);
  font-weight: 600;
  border-radius: var(--radius-md);
  cursor: pointer;
  border: none;
  transition: background var(--transition-fast);
}
.list-cart-btn:hover:not(:disabled) {
  background: var(--color-dark-hover);
}
.list-cart-btn:disabled {
  background: var(--color-border);
  cursor: not-allowed;
}

/* Skeleton */
.skeleton-grid.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
}
.skeleton-grid.list .skeleton-card {
  display: flex;
  gap: var(--space-5);
  padding: var(--space-4);
  background: var(--color-surface);
  border-radius: var(--radius-lg);
}
.skeleton-card {
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--color-surface);
}
.sk-img {
  aspect-ratio: 4/3;
  background: var(--color-border-light);
  animation: shimmer 1.5s infinite;
}
.skeleton-grid.list .sk-img {
  width: 180px;
  aspect-ratio: auto;
  height: 140px;
}
.sk-info {
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  flex: 1;
}
.sk-line {
  height: 14px;
  background: var(--color-border-light);
  border-radius: 4px;
  animation: shimmer 1.5s infinite;
}
.sk-line.short {
  width: 40%;
}
.sk-line.medium {
  width: 25%;
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

/* Empty */
.empty-state {
  text-align: center;
  padding: var(--space-16) 0;
}
.empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: var(--space-4);
}
.empty-state p {
  font-size: var(--text-md);
  color: var(--color-text-secondary);
}
.empty-tip {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary) !important;
  margin-top: var(--space-2);
}
.clear-filters-btn,
.back-home-btn {
  display: inline-block;
  margin-top: var(--space-4);
  padding: var(--space-2) var(--space-6);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  text-decoration: none;
  cursor: pointer;
  transition: all var(--transition-fast);
  background: transparent;
}
.clear-filters-btn:hover,
.back-home-btn:hover {
  border-color: var(--color-dark);
  color: var(--color-text-primary);
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: var(--space-8) 0;
}

/* Loading state */
.loading-state {
  padding: var(--space-4) 0;
}

@media (max-width: 1024px) {
  .cat-layout.with-sidebar {
    grid-template-columns: 1fr;
  }
  .filter-sidebar {
    display: none;
  }
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .skeleton-grid.grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 640px) {
  .sort-group {
    overflow-x: auto;
  }
  .category-container {
    padding: var(--space-4);
  }
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-3);
  }
  .list-item {
    flex-direction: column;
  }
  .list-img-wrap {
    width: 100%;
    height: 200px;
  }
  .cat-banner {
    padding: var(--space-5);
  }
}
</style>

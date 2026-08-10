<template>
  <div class="dashboard-page">
    <h2 class="page-title">数据概览</h2>

    <div class="stat-cards" v-loading="statsLoading">
      <div class="stat-card">
        <div class="stat-icon users">
          <el-icon :size="24">
            <User />
          </el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ stats.userCount }}</div>
          <div class="stat-label">用户总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon furniture">
          <el-icon :size="24">
            <Present />
          </el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ stats.furnitureCount }}</div>
          <div class="stat-label">家具总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orders">
          <el-icon :size="24">
            <Document />
          </el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ stats.orderCount }}</div>
          <div class="stat-label">订单总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon revenue">
          <el-icon :size="24">
            <Money />
          </el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">¥{{ totalAmountDisplay }}</div>
          <div class="stat-label">成交总额</div>
        </div>
      </div>
    </div>

    <div class="chart-section">
      <el-card class="chart-card">
        <template #header>
          <span class="card-header">近7天订单趋势</span>
        </template>
        <div v-loading="trendLoading" class="chart-body">
          <div
            v-if="orderTrend.length === 0 && !trendLoading"
            class="chart-empty"
          >
            暂无订单数据
          </div>
          <div v-else class="bar-chart">
            <div class="bar-item" v-for="item in orderTrend" :key="item.date">
              <div class="bar-value">{{ item.count }}</div>
              <div class="bar-fill-wrap">
                <div
                  class="bar-fill"
                  :style="{ height: barHeight(item.count) }"
                ></div>
              </div>
              <div class="bar-label">{{ item.date.slice(5) }}</div>
            </div>
          </div>
        </div>
      </el-card>
      <el-card class="chart-card">
        <template #header>
          <span class="card-header">热销家具 TOP5</span>
        </template>
        <div v-loading="topLoading" class="chart-body">
          <div
            v-if="topFurniture.length === 0 && !topLoading"
            class="chart-empty"
          >
            暂无销售数据
          </div>
          <div v-else class="rank-list">
            <div
              class="rank-item"
              v-for="(item, index) in topFurniture"
              :key="item.furnitureId"
            >
              <span class="rank-num" :class="'rank-' + (index + 1)">{{
                index + 1
              }}</span>
              <img
                :src="
                  imgUrl(item.furnitureIcon, '/images/default-furniture.png')
                "
                class="rank-img"
                @error="handleRankImgError"
              />
              <div class="rank-info">
                <span class="rank-name">{{ item.furnitureName }}</span>
              </div>
              <div class="rank-sold">
                <span class="sold-count">{{ item.totalSold }}</span>
                <span class="sold-label">件已售</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-card class="low-stock-card" v-if="lowStockList.length > 0">
      <template #header>
        <div class="low-stock-header">
          <div class="low-stock-title">
            <span class="low-stock-dot"></span>
            <span class="card-header">库存预警</span>
            <span class="low-stock-count">{{ lowStockList.length }} 种库存不足</span>
          </div>
          <el-button
            v-if="lowStockList.length > LOW_STOCK_LIMIT"
            type="primary"
            plain
            size="small"
            @click="openLowStockDrawer"
          >
            查看全部
            <el-icon style="margin-left: 2px"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </template>

      <!-- 首页精简卡片：网格展示前 6 个 -->
      <div class="low-stock-grid">
        <div
          class="low-stock-tile"
          v-for="item in visibleLowStock"
          :key="item.id"
          :class="{ soldout: item.stock === 0 }"
        >
          <div class="tile-thumb">
            <img
              :src="imgUrl(item.fIcon, '/images/default-furniture.png')"
              alt=""
              @error="handleLowStockImgError"
            />
          </div>
          <div class="tile-body">
            <span class="tile-name">{{ item.fName }}</span>
            <span class="tile-type">{{ item.typeName || "未分类" }}</span>
          </div>
          <div class="tile-stock" :class="item.stock === 0 ? 'is-out' : 'is-low'">
            {{ item.stock === 0 ? "售罄" : "剩 " + item.stock }}
          </div>
        </div>
      </div>
    </el-card>

    <!-- 库存不足详情抽屉 -->
    <el-drawer
      v-model="lowStockDrawerVisible"
      title="库存预警明细"
      size="55%"
      :with-header="true"
      class="low-stock-drawer"
    >
      <div class="drawer-content">
        <!-- 筛选条 -->
        <div class="drawer-filter">
          <el-segmented
            v-model="stockFilter"
            :options="stockFilterOptions"
            size="default"
          />
          <el-select
            v-model="typeFilter"
            placeholder="全部种类"
            clearable
            size="default"
            style="width: 150px"
          >
            <el-option
              v-for="t in typeOptions"
              :key="t"
              :label="t"
              :value="t"
            />
          </el-select>
        </div>

        <!-- 结果统计 -->
        <div class="drawer-summary">
          共 <b>{{ filteredLowStock.length }}</b> 种符合筛选
        </div>

        <!-- 商品列表 -->
        <div class="drawer-list" v-if="filteredLowStock.length > 0">
          <div
            class="drawer-item"
            v-for="item in filteredLowStock"
            :key="item.id"
            :class="{ 'is-out': item.stock === 0 }"
          >
            <div class="d-item-thumb">
              <img
                :src="imgUrl(item.fIcon, '/images/default-furniture.png')"
                alt=""
                @error="handleLowStockImgError"
              />
            </div>
            <div class="d-item-info">
              <div class="d-item-name">{{ item.fName }}</div>
              <div class="d-item-meta">
                <span class="d-item-type">{{ item.typeName || "未分类" }}</span>
              </div>
            </div>
            <div class="d-item-right">
              <span
                class="d-item-stock"
                :class="item.stock === 0 ? 'is-out' : 'is-low'"
              >
                {{ item.stock === 0 ? "已售罄" : "仅剩 " + item.stock + " 件" }}
              </span>
              <div class="d-item-bar">
                <span
                  class="bar-fill"
                  :class="item.stock === 0 ? 'is-out' : 'is-low'"
                  :style="{ width: stockPercent(item.stock) + '%' }"
                ></span>
              </div>
            </div>
          </div>
        </div>

        <el-empty
          v-else
          description="没有符合筛选的库存不足商品"
          :image-size="80"
        />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import {
  ArrowRight,
  Document,
  Money,
  Present,
  User,
} from "@element-plus/icons-vue";
import {
  getDashboardStats,
  getLowStock,
  getOrderTrend,
  getTopFurniture,
} from "@/api/admin/dashboard.js";
import { imgUrl } from "@/utils/img.js";

const statsLoading = ref(false);
const stats = ref({
  userCount: 0,
  furnitureCount: 0,
  orderCount: 0,
  totalAmount: 0,
});

const totalAmountDisplay = computed(() => {
  const num = Number(stats.value.totalAmount);
  return num.toLocaleString("zh-CN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
});

const trendLoading = ref(false);
const orderTrend = ref([]);

const maxTrendCount = computed(() => {
  const nums = orderTrend.value.map((i) => i.count);
  return nums.length > 0 ? Math.max(...nums) : 0;
});

const barHeight = (count) => {
  if (maxTrendCount.value === 0) return "8%";
  const pct = (count / maxTrendCount.value) * 100;
  return Math.max(pct, count > 0 ? 4 : 0) + "%";
};

const topLoading = ref(false);
const topFurniture = ref([]);

const lowStockList = ref([]);

// 库存预警：首页默认只显示前 6 个，点击"查看全部"打开抽屉展示明细
const LOW_STOCK_LIMIT = 6;
const visibleLowStock = computed(() => lowStockList.value.slice(0, LOW_STOCK_LIMIT));

// ===== 库存预警抽屉 =====
const lowStockDrawerVisible = ref(false);
const stockFilter = ref("全部");
const typeFilter = ref("");

const stockFilterOptions = ["全部", "已售罄", "库存告警"];

// 分类选项：从数据中动态提取去重
const typeOptions = computed(() => {
  const set = new Set(lowStockList.value.map((i) => i.typeName).filter(Boolean));
  return [...set];
});

const openLowStockDrawer = () => {
  stockFilter.value = "全部";
  typeFilter.value = "";
  lowStockDrawerVisible.value = true;
};

// 筛选后的列表
const filteredLowStock = computed(() => {
  return lowStockList.value.filter((item) => {
    if (stockFilter.value === "已售罄" && item.stock !== 0) return false;
    if (stockFilter.value === "库存告警" && (item.stock === 0 || item.stock >= 10)) return false;
    if (typeFilter.value && item.typeName !== typeFilter.value) return false;
    return true;
  });
});

// 库存占比条：0→100%，满库存 10 → 100%
const stockPercent = (stock) => {
  const p = Math.max(0, Math.min(100, (stock / 10) * 100));
  return p;
};

const handleRankImgError = (e) => {
  e.target.src = "/images/default-furniture.png";
};

const handleLowStockImgError = (e) => {
  e.target.style.display = "none";
};

onMounted(async () => {
  statsLoading.value = true;
  trendLoading.value = true;
  topLoading.value = true;

  try {
    const [statsRes, trendRes, topRes, lowRes] = await Promise.all([
      getDashboardStats().catch((err) => ({ success: false, error: err })),
      getOrderTrend().catch((err) => ({ success: false, error: err })),
      getTopFurniture().catch((err) => ({ success: false, error: err })),
      getLowStock().catch((err) => ({ success: false, error: err })),
    ]);

    if (statsRes.success && statsRes.data) {
      stats.value = statsRes.data;
    }

    if (trendRes.success && Array.isArray(trendRes.data)) {
      orderTrend.value = trendRes.data;
    }

    if (topRes.success && Array.isArray(topRes.data)) {
      topFurniture.value = topRes.data;
    }

    if (lowRes.success && Array.isArray(lowRes.data)) {
      lowStockList.value = lowRes.data;
    }
  } finally {
    statsLoading.value = false;
    trendLoading.value = false;
    topLoading.value = false;
  }
});
</script>

<style scoped>
.page-title {
  margin: 0 0 24px 0;
  font-size: 20px;
  color: #333;
}

/* ===== 统计卡片 ===== */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
  min-height: 104px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  border-color: #e0e0e0;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon.users {
  background: #eef2f7;
  color: #5a7fa0;
}

.stat-icon.furniture {
  background: #faf3eb;
  color: #b8753e;
}

.stat-icon.orders {
  background: #eef5f0;
  color: #5b8c5a;
}

.stat-icon.revenue {
  background: #fef5f5;
  color: #c5554a;
}

.stat-body {
  flex: 1;
  min-width: 0;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

/* ===== 图表区域 ===== */
.chart-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card {
  min-height: 300px;
}

.chart-card :deep(.el-card__body) {
  padding: 0 20px 20px;
}

.card-header {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.chart-body {
  min-height: 240px;
}

.chart-empty {
  height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bbb;
  font-size: 14px;
  background: #fafbfc;
  border-radius: 8px;
}

/* ===== 柱状图 ===== */
.bar-chart {
  display: flex;
  align-items: flex-end;
  height: 240px;
  padding: 0 8px;
  gap: 8px;
}

.bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  height: 100%;
  min-width: 0;
}

.bar-fill-wrap {
  width: 100%;
  max-width: 40px;
  min-width: 20px;
  height: 190px;
  display: flex;
  flex-direction: column-reverse;
  background: #f5f6f8;
  border-radius: 4px;
}

.bar-fill {
  width: 100%;
  background: #2c2c2c;
  border-radius: 4px 4px 0 0;
  min-height: 2px;
  transition: height 0.5s ease;
}

.bar-label {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
  text-align: center;
  line-height: 1;
}

.bar-value {
  font-size: 12px;
  color: #2c2c2c;
  font-weight: 600;
  margin-bottom: 6px;
  line-height: 1;
}

/* ===== 排行榜 ===== */
.rank-list {
  min-height: 240px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.rank-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  gap: 12px;
  border-bottom: 1px solid #f5f5f5;
}

.rank-item:last-child {
  border-bottom: none;
}

.rank-num {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: #999;
  background: #f0f0f0;
  flex-shrink: 0;
}

.rank-num.rank-1 {
  color: #fff;
  background: #b8753e;
}

.rank-num.rank-2 {
  color: #fff;
  background: #8a9aa8;
}

.rank-num.rank-3 {
  color: #fff;
  background: #b8a088;
}

.rank-img {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  object-fit: cover;
  background: #f0f2f5;
  flex-shrink: 0;
}

.rank-info {
  flex: 1;
  min-width: 0;
}

.rank-name {
  font-size: 14px;
  color: #333;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rank-sold {
  text-align: right;
  flex-shrink: 0;
}

.sold-count {
  font-size: 16px;
  font-weight: 700;
  color: #2c2c2c;
}

.sold-label {
  font-size: 12px;
  color: #bbb;
  margin-left: 4px;
}

/* ===== 库存预警 ===== */
.low-stock-card {
  margin-top: 20px;
}

.low-stock-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.low-stock-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.low-stock-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e0584f;
  box-shadow: 0 0 0 3px rgba(224, 88, 79, 0.15);
}

.low-stock-count {
  font-size: 12px;
  color: #999;
  font-weight: 400;
}

/* 首页精简网格卡片 */
.low-stock-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.low-stock-tile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: linear-gradient(135deg, #fffdfc 0%, #fff 100%);
  border: 1px solid #f3e3de;
  border-radius: 10px;
  transition: all 0.2s ease;
}
.low-stock-tile:hover {
  border-color: #e8c4bc;
  box-shadow: 0 3px 10px rgba(224, 88, 79, 0.06);
  transform: translateY(-1px);
}
.low-stock-tile.soldout {
  background: #fbf7f6;
}

.tile-thumb {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f2ef;
}
.tile-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.tile-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.tile-name {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.tile-type {
  font-size: 11px;
  color: #aaa;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tile-stock {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 12px;
  flex-shrink: 0;
}
.tile-stock.is-low {
  color: #d4862b;
  background: #fdf3e3;
}
.tile-stock.is-out {
  color: #d9534f;
  background: #fdeceb;
}

/* ===== 库存预警抽屉 ===== */
.drawer-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.drawer-filter {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.drawer-summary {
  font-size: 13px;
  color: #666;
}
.drawer-summary b {
  color: #d9534f;
  font-size: 15px;
}

.drawer-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.drawer-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #f0ece8;
  border-radius: 10px;
  transition: all 0.15s ease;
}
.drawer-item:hover {
  border-color: #e0d6ce;
  background: #fdfbfa;
}
.drawer-item.is-out {
  background: #fbf7f6;
}

.d-item-thumb {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f2ef;
}
.d-item-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.d-item-info {
  flex: 1;
  min-width: 0;
}
.d-item-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.d-item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 3px;
  font-size: 12px;
  color: #aaa;
}
.d-item-type {
  color: #7a6a5a;
  background: #f3efe9;
  padding: 1px 8px;
  border-radius: 10px;
}

.d-item-right {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  width: 110px;
}
.d-item-stock {
  font-size: 12px;
  font-weight: 600;
}
.d-item-stock.is-low {
  color: #d4862b;
}
.d-item-stock.is-out {
  color: #d9534f;
}

/* 库存占比条 */
.d-item-bar {
  width: 100%;
  height: 5px;
  border-radius: 3px;
  background: #f1ede9;
  overflow: hidden;
}
.bar-fill {
  display: block;
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}
.bar-fill.is-low {
  background: linear-gradient(90deg, #f0b45a, #d4862b);
}
.bar-fill.is-out {
  background: linear-gradient(90deg, #e8837f, #d9534f);
}

@media (max-width: 1200px) {
  .stat-cards,
  .chart-section {
    grid-template-columns: repeat(2, 1fr);
  }
  .low-stock-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stat-cards,
  .chart-section {
    grid-template-columns: 1fr;
  }
  .low-stock-grid {
    grid-template-columns: 1fr;
  }
  .drawer-filter {
    flex-direction: column;
    align-items: stretch;
  }
  .drawer-filter .el-select {
    width: 100% !important;
  }
}
</style>

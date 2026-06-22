<template>
  <div class="dashboard-page">
    <h2 class="page-title">数据概览</h2>

    <div class="stat-cards" v-loading="statsLoading">
      <div class="stat-card">
        <div class="stat-icon users">
          <el-icon :size="24">
            <User/>
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
            <Present/>
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
            <Document/>
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
            <Money/>
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
          <div v-if="orderTrend.length === 0 && !trendLoading" class="chart-empty">暂无订单数据</div>
          <div v-else class="bar-chart">
            <div class="bar-item" v-for="item in orderTrend" :key="item.date">
              <div class="bar-value">{{ item.count }}</div>
              <div class="bar-fill-wrap">
                <div class="bar-fill" :style="{ height: barHeight(item.count) }"></div>
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
          <div v-if="topFurniture.length === 0 && !topLoading" class="chart-empty">暂无销售数据</div>
          <div v-else class="rank-list">
            <div class="rank-item" v-for="(item, index) in topFurniture" :key="item.furnitureId">
              <span class="rank-num" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
              <img :src="imgUrl(item.furnitureIcon, '/images/default-furniture.png')"
                   class="rank-img"
                   @error="handleRankImgError"/>
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
        <span class="card-header" style="color: #d95a5a;">库存预警 (库存不足10件)</span>
      </template>
      <div class="low-stock-list">
        <div class="low-stock-item" v-for="item in lowStockList" :key="item.id">
          <img :src="imgUrl(item.f_icon, '/images/default-furniture.png')"
               class="low-stock-img" @error="handleLowStockImgError"/>
          <span class="low-stock-name">{{ item.f_name }}</span>
          <el-tag :type="item.stock === 0 ? 'danger' : 'warning'" size="small">
            {{ item.stock === 0 ? '已售罄' : '仅剩 ' + item.stock + ' 件' }}
          </el-tag>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {Document, Money, Present, User} from '@element-plus/icons-vue'
import {getDashboardStats, getLowStock, getOrderTrend, getTopFurniture} from '@/api/admin/dashboard.js'
import {imgUrl} from '@/utils/img.js'

const statsLoading = ref(false)
const stats = ref({
  userCount: 0,
  furnitureCount: 0,
  orderCount: 0,
  totalAmount: 0
})

const totalAmountDisplay = computed(() => {
  const num = Number(stats.value.totalAmount)
  return num.toLocaleString('zh-CN', {minimumFractionDigits: 2, maximumFractionDigits: 2})
})

const trendLoading = ref(false)
const orderTrend = ref([])

const maxTrendCount = computed(() => {
  const nums = orderTrend.value.map(i => i.count)
  return nums.length > 0 ? Math.max(...nums) : 0
})

const barHeight = (count) => {
  if (maxTrendCount.value === 0) return '8%'
  const pct = (count / maxTrendCount.value) * 100
  return Math.max(pct, count > 0 ? 4 : 0) + '%'
}

const topLoading = ref(false)
const topFurniture = ref([])

const lowStockList = ref([])

const handleRankImgError = (e) => {
  e.target.src = '/images/default-furniture.png'
}

const handleLowStockImgError = (e) => {
  e.target.style.display = 'none'
}

onMounted(async () => {
  statsLoading.value = true
  trendLoading.value = true
  topLoading.value = true

  try {
    const [statsRes, trendRes, topRes, lowRes] = await Promise.all([
      getDashboardStats().catch(err => ({success: false, error: err})),
      getOrderTrend().catch(err => ({success: false, error: err})),
      getTopFurniture().catch(err => ({success: false, error: err})),
      getLowStock().catch(err => ({success: false, error: err}))
    ])

    if (statsRes.success && statsRes.data) {
      stats.value = statsRes.data
    }

    if (trendRes.success && Array.isArray(trendRes.data)) {
      orderTrend.value = trendRes.data
    }

    if (topRes.success && Array.isArray(topRes.data)) {
      topFurniture.value = topRes.data
    }

    if (lowRes.success && Array.isArray(lowRes.data)) {
      lowStockList.value = lowRes.data
    }
  } finally {
    statsLoading.value = false
    trendLoading.value = false
    topLoading.value = false
  }
})
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
  background: #e8f0fe;
  color: #4a7dcc;
}

.stat-icon.furniture {
  background: #fef0e4;
  color: #d98a4a;
}

.stat-icon.orders {
  background: #e6f2ec;
  color: #5c9a78;
}

.stat-icon.revenue {
  background: #fce8ec;
  color: #cc5a6a;
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
  background: #5a6a7a;
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
  color: #5a6a7a;
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
  background: #d98a4a;
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
  color: #5a6a7a;
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

.low-stock-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.low-stock-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px;
  background: #fef8f6;
  border-radius: 8px;
  border: 1px solid #fde2d8;
}

.low-stock-img {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  object-fit: cover;
  background: #f5f6f8;
}

.low-stock-name {
  font-size: 14px;
  color: #333;
  max-width: 160px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@media (max-width: 1200px) {
  .stat-cards,
  .chart-section {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stat-cards,
  .chart-section {
    grid-template-columns: 1fr;
  }
}
</style>

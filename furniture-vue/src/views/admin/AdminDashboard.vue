<template>
    <div class="dashboard-page">
        <h2 class="page-title">📊 数据概览</h2>

        <div class="stat-cards">
            <div class="stat-card blue">
                <div class="stat-number">{{ stats.userCount }}</div>
                <div class="stat-label">用户总数</div>
            </div>
            <div class="stat-card purple">
                <div class="stat-number">{{ stats.furnitureCount }}</div>
                <div class="stat-label">家具总数</div>
            </div>
            <div class="stat-card cyan">
                <div class="stat-number">{{ stats.orderCount }}</div>
                <div class="stat-label">订单总数</div>
            </div>
            <div class="stat-card green">
                <div class="stat-number">¥{{ stats.totalAmount }}</div>
                <div class="stat-label">成交总额</div>
            </div>
        </div>

        <!-- 简单图表区域（后续可扩展） -->
        <div class="chart-section">
            <el-card class="chart-card">
                <template #header>近7天订单趋势</template>
                <div class="placeholder-chart">📈 图表区域（后续接入）</div>
            </el-card>
            <el-card class="chart-card">
                <template #header>热销家具TOP5</template>
                <div class="placeholder-chart">🏆 排行榜区域（后续接入）</div>
            </el-card>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const stats = ref({
    userCount: 0,
    furnitureCount: 0,
    orderCount: 0,
    totalAmount: '0.00'
})

// TODO: 调用统计接口
onMounted(() => {
    // 模拟数据
    stats.value = {
        userCount: 128,
        furnitureCount: 56,
        orderCount: 342,
        totalAmount: '45,680.00'
    }
})
</script>

<style scoped>
.page-title {
    margin: 0 0 24px 0;
    font-size: 20px;
    color: #333;
}

.stat-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 24px;
}

.stat-card {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    color: #fff;
}

.stat-card.blue {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.purple {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card.cyan {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-card.green {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-number {
    font-size: 32px;
    font-weight: 700;
    margin-bottom: 8px;
}

.stat-label {
    font-size: 14px;
    opacity: 0.9;
}

.chart-section {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
}

.chart-card {
    min-height: 300px;
}

.placeholder-chart {
    height: 240px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    font-size: 16px;
    background: #f9f9f9;
    border-radius: 8px;
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
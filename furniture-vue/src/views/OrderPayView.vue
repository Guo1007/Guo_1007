<template>
    <div class="pay-page">
        <!-- 顶部导航 -->
        <div class="top-nav">
            <div class="nav-content">
                <el-button text @click="goBack" class="back-btn">
                    <el-icon>
                        <ArrowLeft />
                    </el-icon>
                    返回订单
                </el-button>
                <div class="breadcrumb">订单支付</div>
            </div>
        </div>

        <div class="pay-container">
            <!-- 支付卡片 -->
            <el-card class="pay-card" shadow="never">
                <div class="pay-header">
                    <div class="pay-icon">💳</div>
                    <h2>订单支付</h2>
                    <p class="order-no">订单号：{{ orderId }}</p>
                </div>

                <!-- 订单信息 -->
                <div class="order-info" v-if="orderInfo">
                    <div class="info-row">
                        <span>收货人</span>
                        <span>{{ orderInfo.consignee }}</span>
                    </div>
                    <div class="info-row">
                        <span>联系电话</span>
                        <span>{{ orderInfo.phone }}</span>
                    </div>
                    <div class="info-row">
                        <span>收货地址</span>
                        <span>{{ orderInfo.address }}</span>
                    </div>
                </div>

                <el-divider />

                <!-- 商品列表 -->
                <div class="goods-list" v-if="orderInfo && orderInfo.itemList">
                    <h4>商品明细</h4>
                    <div v-for="item in orderInfo.itemList" :key="item.id" class="goods-item">
                        <span class="name">{{ item.furnitureName }}</span>
                        <span class="count">×{{ item.quantity }}</span>
                        <span class="price">¥{{ formatPrice(item.itemTotalPrice) }}</span>
                    </div>
                </div>

                <el-divider />

                <!-- 支付金额 -->
                <div class="pay-amount">
                    <span>应付金额</span>
                    <span class="amount">¥{{ formatPrice(orderInfo?.totalPrice) }}</span>
                </div>

                <!-- 支付方式 -->
                <div class="pay-method">
                    <h4>选择支付方式</h4>
                    <div class="method-options">
                        <div class="method-item" :class="{ active: payMethod === 'wechat' }"
                            @click="payMethod = 'wechat'">
                            <span class="icon"></span>
                            <span>微信支付</span>
                        </div>
                        <div class="method-item" :class="{ active: payMethod === 'alipay' }"
                            @click="payMethod = 'alipay'">
                            <span class="icon"></span>
                            <span>支付宝</span>
                        </div>
                    </div>
                </div>

                <!-- 支付按钮 -->
                <div class="pay-action">
                    <el-button type="primary" size="large" :loading="paying" @click="handlePay" class="pay-btn">
                        确认支付 ¥{{ formatPrice(orderInfo?.totalPrice) }}
                    </el-button>
                    <el-button text @click="cancelPay" class="cancel-btn">
                        取消支付
                    </el-button>
                </div>
            </el-card>

            <!-- 支付成功弹窗 -->
            <el-dialog v-model="successDialogVisible" title="支付成功" width="400px" :close-on-click-modal="false"
                :show-close="false" center>
                <div class="success-content">
                    <div class="success-icon">✓</div>
                    <p class="success-text">支付成功！</p>
                    <p class="success-tip">感谢您的购买，我们将尽快为您发货</p>
                </div>
                <template #footer>
                    <el-button type="primary" @click="goToOrders" size="large">
                        查看订单
                    </el-button>
                    <el-button @click="goHome" size="large">返回首页</el-button>
                </template>
            </el-dialog>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getOrderDetail, payOrder } from '@/api/order.js'
import '@/styles/views/payView.scss'

const route = useRoute()
const router = useRouter()
const orderId = ref(route.params.id)

const orderInfo = ref({})
const payMethod = ref('wechat')
const paying = ref(false)
const successDialogVisible = ref(false)

// 加载订单信息
const loadOrderInfo = async () => {
    try {
        const res = await getOrderDetail(orderId.value)
        if (res.success || res.code === 200) {
            orderInfo.value = res.data
            if (res.data.status !== 0) {
                ElMessage.info('该订单已支付或已取消')
                router.push('/user/orders')
            }
        } else {
            ElMessage.error(res.message || '获取订单失败')
            router.push('/user/orders')
        }
    } catch (error) {
        console.error('加载订单失败:', error)
        ElMessage.error('加载订单失败')
        router.push('/user/orders')
    }
}

const formatPrice = (price) => {
    if (!price) return '0.00'
    return parseFloat(price).toFixed(2)
}

const handlePay = async () => {
    paying.value = true
    try {
        // 模拟支付延迟
        await new Promise(resolve => setTimeout(resolve, 1500))

        const res = await payOrder(orderId.value)
        if (res.success || res.code === 200) {
            successDialogVisible.value = true
        } else {
            ElMessage.error(res.message || '支付失败')
        }
    } catch (error) {
        console.error('支付失败:', error)
        ElMessage.error('支付失败，请重试')
    } finally {
        paying.value = false
    }
}

const cancelPay = () => {
    ElMessage.info('您已取消支付')
    router.push('/user/orders')
}

const goBack = () => {
    router.back()
}

const goToOrders = () => {
    successDialogVisible.value = false
    router.push('/user/orders')
}

const goHome = () => {
    successDialogVisible.value = false
    router.push('/')
}

onMounted(() => {
    loadOrderInfo()
})
</script>
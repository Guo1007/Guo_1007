<template>
    <div class="orders-page">
        <!-- 顶部导航 -->
        <div class="top-nav">
            <div class="nav-content">
                <el-button text @click="goBack" class="back-btn">
                    <el-icon>
                        <ArrowLeft />
                    </el-icon>
                    返回个人中心
                </el-button>
                <div class="breadcrumb">个人中心 / 购买记录</div>
            </div>
        </div>

        <div class="orders-container">
            <!-- 页面标题 -->
            <div class="page-header">
                <h1>我的订单</h1>
                <p>查看您的历史购买记录及订单详情</p>
            </div>

            <!-- 订单列表 -->
            <div class="orders-list" v-if="loading">
                <div class="loading-state">
                    <div class="spinner"></div>
                    <p>加载订单中...</p>
                </div>
            </div>

            <div class="orders-list" v-else-if="orderList.length === 0">
                <el-empty description="暂无订单记录">
                    <el-button type="primary" @click="goHome">去逛逛</el-button>
                </el-empty>
            </div>

            <div class="orders-list" v-else>
                <el-card v-for="order in orderList" :key="order.id" class="order-card" shadow="hover">
                    <!-- 订单头部 -->
                    <div class="order-header">
                        <div class="order-info">
                            <span class="order-no">订单号：{{ order.id }}</span>
                            <span class="order-time">{{ formatTime(order.createTime) }}</span>
                        </div>
                        <el-tag :type="getStatusType(order.status)" size="small" effect="dark">
                            {{ getStatusText(order.status) }}
                        </el-tag>
                    </div>

                    <!-- 订单商品列表 -->
                    <div class="order-items">
                        <div v-for="item in (order.itemList || [])" :key="item.id" class="order-item"
                            @click="goToFurniture(item.furnitureId)">
                            <img :src="item.furnitureIcon ? 'http://localhost:8080' + item.furnitureIcon : '/images/default-furniture.png'"
                                class="item-img" @error="handleImgError" />
                            <div class="item-info">
                                <h4>{{ item.furnitureName }}</h4>
                                <p class="item-price">¥{{ formatPrice(item.price) }} × {{ item.quantity }}</p>
                            </div>
                            <div class="item-total">
                                ¥{{ formatPrice(item.itemTotalPrice) }}
                            </div>
                        </div>
                    </div>

                    <!-- 订单底部 -->
                    <div class="order-footer">
                        <div class="delivery-info">
                            <p><el-icon>
                                    <User />
                                </el-icon> {{ order.consignee }} {{ order.phone }}</p>
                            <p><el-icon>
                                    <Location />
                                </el-icon> {{ order.address }}</p>
                        </div>
                        <div class="order-summary">
                            <div class="total-row">
                                <span>共 {{ getTotalCount(order.itemList || []) }} 件商品</span>
                                <span class="total-price">实付：¥{{ formatPrice(order.totalPrice) }}</span>
                            </div>
                            <div class="order-actions">
                                <!-- 待支付：支付 + 取消 -->
                                <el-button v-if="order.status === 0" type="primary" size="small"
                                    @click="payOrder(order.id)">
                                    立即支付
                                </el-button>
                                <el-button v-if="order.status === 0" type="danger" plain size="small"
                                    @click="cancelOrder(order.id)">
                                    取消订单
                                </el-button>

                                <!-- 已发货：确认收货 + 查看详情 -->
                                <el-button v-if="order.status === 2" type="success" size="small"
                                    @click="confirmReceipt(order)">
                                    <el-icon>
                                        <Check />
                                    </el-icon>
                                    确认收货
                                </el-button>

                                <!-- 已发货/已完成：查看详情 -->
                                <el-button v-if="order.status === 2 || order.status === 3" type="info" plain
                                    size="small" @click="viewDetail(order)">
                                    查看详情
                                </el-button>
                            </div>
                        </div>
                    </div>
                </el-card>

                <!-- 分页 -->
                <div class="pagination-wrapper" v-if="total > 0">
                    <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
                        :page-sizes="[5, 10, 20]" layout="total, sizes, prev, pager, next"
                        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
                </div>
            </div>
        </div>

        <!-- 订单详情弹窗 -->
        <el-dialog v-model="detailDialogVisible" title="订单详情" width="600px" :close-on-click-modal="false">
            <div v-if="currentOrder" class="order-detail-content">
                <div class="detail-section">
                    <h4>订单信息</h4>
                    <p><span>订单编号：</span>{{ currentOrder.id }}</p>
                    <p><span>下单时间：</span>{{ formatTime(currentOrder.createTime) }}</p>
                    <p><span>订单状态：</span>
                        <el-tag :type="getStatusType(currentOrder.status)" size="small">
                            {{ getStatusText(currentOrder.status) }}
                        </el-tag>
                    </p>
                    <p v-if="currentOrder.payTime"><span>支付时间：</span>{{ formatTime(currentOrder.payTime) }}</p>
                    <p v-if="currentOrder.shipTime"><span>发货时间：</span>{{ formatTime(currentOrder.shipTime) }}</p>
                    <p v-if="currentOrder.receiveTime"><span>收货时间：</span>{{ formatTime(currentOrder.receiveTime) }}</p>
                </div>

                <el-divider />

                <div class="detail-section">
                    <h4>收货信息</h4>
                    <p><span>收货人：</span>{{ currentOrder.consignee }}</p>
                    <p><span>联系电话：</span>{{ currentOrder.phone }}</p>
                    <p><span>收货地址：</span>{{ currentOrder.address }}</p>
                    <p v-if="currentOrder.remark"><span>订单备注：</span>{{ currentOrder.remark }}</p>
                </div>

                <el-divider />

                <div class="detail-section">
                    <h4>商品明细</h4>
                    <div v-for="item in (currentOrder?.itemList || [])" :key="item.id" class="detail-item">
                        <img :src="item.furnitureIcon ? 'http://localhost:8080' + item.furnitureIcon : '/images/default-furniture.png'"
                            class="detail-item-img" />
                        <div class="detail-item-info">
                            <p class="name">{{ item.furnitureName }}</p>
                            <p class="price">¥{{ formatPrice(item.price) }} × {{ item.quantity }}</p>
                        </div>
                        <div class="detail-item-total">¥{{ formatPrice(item.itemTotalPrice) }}</div>
                    </div>
                </div>

                <el-divider />

                <div class="detail-summary">
                    <div class="summary-row">
                        <span>商品总额</span>
                        <span>¥{{ formatPrice(currentOrder.totalPrice) }}</span>
                    </div>
                    <div class="summary-row">
                        <span>运费</span>
                        <span>¥0.00</span>
                    </div>
                    <div class="summary-row total">
                        <span>实付金额</span>
                        <span class="amount">¥{{ formatPrice(currentOrder.totalPrice) }}</span>
                    </div>
                </div>

                <!-- 弹窗底部操作按钮 -->
                <div v-if="currentOrder.status === 2" class="dialog-actions">
                    <el-button type="success" @click="confirmReceipt(currentOrder); detailDialogVisible = false">
                        <el-icon>
                            <Check />
                        </el-icon>
                        确认收货
                    </el-button>
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, User, Location, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserOrders, cancelOrder as apiCancelOrder, confirmReceipt as apiConfirmReceipt } from '@/api/order.js'
import { getFurnitureById } from '@/api/furniture.js'
import '@/styles/views/userOrder.scss'

const router = useRouter()

const loading = ref(true)
const orderList = ref([])
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)

const detailDialogVisible = ref(false)
const currentOrder = ref(null)

// 加载订单列表
const loadOrders = async () => {
    loading.value = true
    try {
        const res = await getUserOrders({
            page: currentPage.value,
            size: pageSize.value
        })
        if (res.success || res.code === 200) {
            orderList.value = res.data.records || []
            total.value = res.data.total || 0
        } else {
            ElMessage.error(res.message || '获取订单失败')
        }
    } catch (error) {
        console.error('加载订单失败:', error)
        ElMessage.error('加载订单失败')
    } finally {
        loading.value = false
    }
}

// 状态映射
const getStatusText = (status) => {
    const map = {
        0: '待支付',
        1: '已支付',
        2: '已发货',
        3: '已完成',
        4: '已取消'
    }
    return map[status] || '未知状态'
}

const getStatusType = (status) => {
    const map = {
        0: 'warning',
        1: 'success',
        2: 'primary',
        3: 'info',
        4: 'danger'
    }
    return map[status] || 'info'
}

// 计算商品总数
const getTotalCount = (items) => {
    if (!items || !Array.isArray(items)) return 0
    return items.reduce((sum, item) => sum + item.quantity, 0)
}

// 格式化价格
const formatPrice = (price) => {
    if (!price) return '0.00'
    return parseFloat(price).toFixed(2)
}

// 格式化时间
const formatTime = (time) => {
    if (!time) return '-'
    const date = new Date(time)
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    })
}

// 跳转到商品详情
const goToFurniture = (id) => {
    router.push(`/furniture/detail/${id}`)
    getFurnitureById(id)
}

// 支付订单
const payOrder = (orderId) => {
    router.push(`/order/pay/${orderId}`)
}

// 取消订单
const cancelOrder = async (orderId) => {
    try {
        await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        const res = await apiCancelOrder(orderId)
        if (res.success) {
            ElMessage.success('订单已取消')
            loadOrders()
        } else {
            ElMessage.error(res.message || '取消失败')
        }
    } catch (error) {
        if (error !== 'cancel') {
            console.error('取消订单失败:', error)
            ElMessage.error('取消失败')
        }
    }
}

// 确认收货
const confirmReceipt = async (order) => {
    try {
        await ElMessageBox.confirm(
            `确认已收到订单 "${order.id}" 的商品吗？`,
            '确认收货',
            {
                confirmButtonText: '确认收货',
                cancelButtonText: '取消',
                type: 'success'
            }
        )

        const res = await apiConfirmReceipt(order.id)
        if (res.success || res.code === 200) {
            ElMessage.success('确认收货成功')
            loadOrders() // 刷新列表
        } else {
            ElMessage.error(res.message || res.errorMsg || '确认收货失败')
        }
    } catch (error) {
        if (error !== 'cancel') {
            console.error('确认收货失败:', error)
            ElMessage.error('确认收货失败')
        }
    }
}

// 查看详情
const viewDetail = (order) => {
    currentOrder.value = order
    detailDialogVisible.value = true
}

// 分页处理
const handleSizeChange = (val) => {
    pageSize.value = val
    currentPage.value = 1
    loadOrders()
}

const handleCurrentChange = (val) => {
    currentPage.value = val
    loadOrders()
}

const goBack = () => {
    router.push('/user/profile')
}

const goHome = () => {
    router.push('/')
}

const handleImgError = (e) => {
    e.target.src = '/images/default-furniture.png'
}

onMounted(() => {
    loadOrders()
})
</script>
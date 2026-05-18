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
                          <img :src="imgUrl(item.furnitureIcon, '/images/default-furniture.png')"
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

                              <!-- 已完成：评价 + 查看详情 -->
                              <el-button v-if="canReviewOrder(order)" type="warning" size="small"
                                         @click="openReviewDialog(order)">
                                <el-icon>
                                  <Star/>
                                </el-icon>
                                {{ reviewBtnText(order) }}
                              </el-button>

                              <!-- 已评价：查看评价 -->
                              <el-button v-if="order.status === 5" type="success" size="small"
                                         @click="openReviewManageDialog(order)">
                                <el-icon>
                                  <Star/>
                                </el-icon>
                                查看评价
                              </el-button>

                              <!-- 已发货/已完成/已评价：查看详情 -->
                              <el-button v-if="order.status === 2 || order.status === 3 || order.status === 5"
                                         type="info" plain
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

      <!-- 评价弹窗 -->
      <el-dialog v-model="reviewDialogVisible" title="商品评价" width="480px" :close-on-click-modal="false">
        <div v-if="reviewTarget" class="review-dialog-body">
          <el-form label-position="top">
            <el-form-item label="评价商品">
              <el-select v-model="reviewForm.furnitureId" placeholder="选择要评价的商品" style="width:100%">
                <el-option v-for="item in unreviewedItems(reviewTarget)" :key="item.furnitureId"
                           :label="item.furnitureName" :value="item.furnitureId"/>
              </el-select>
            </el-form-item>
            <el-form-item label="评分">
              <el-rate v-model="reviewForm.rating" :max="5" show-score/>
            </el-form-item>
            <el-form-item label="评价内容">
              <el-input v-model="reviewForm.content" type="textarea" :rows="4"
                        placeholder="分享你的使用体验..." maxlength="500" show-word-limit/>
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReview" :loading="reviewSubmitting">提交评价</el-button>
        </template>
      </el-dialog>

      <!-- 评价管理弹窗（查看/追评/删评） -->
      <el-dialog v-model="reviewManageVisible" title="评价管理" width="560px" :close-on-click-modal="false">
        <div v-if="reviewManageOrder" class="review-manage-body">
          <div class="review-manage-section" v-if="existingReviews.length > 0">
            <h4>已有评价（{{ existingReviews.length }}条）</h4>
            <div v-for="r in existingReviews" :key="r.id" class="review-manage-card">
              <div class="review-manage-card-hd">
                <span class="review-manage-stars">{{ '⭐'.repeat(r.rating) }}</span>
                <span class="review-manage-time">{{ formatTime(r.createTime) }}</span>
              </div>
              <p class="review-manage-content" v-if="r.content">{{ r.content }}</p>
              <el-button type="danger" text size="small" @click="handleDeleteReview(r.id)">
                <el-icon>
                  <Delete/>
                </el-icon>
                删除
              </el-button>
            </div>
          </div>
          <el-divider v-if="existingReviews.length > 0 && unreviewedItems(reviewManageOrder).length > 0"/>
          <div class="review-manage-section" v-if="unreviewedItems(reviewManageOrder).length > 0">
            <h4>追加评价</h4>
            <el-form label-position="top">
              <el-form-item label="评价商品">
                <el-select v-model="reviewManageForm.furnitureId" placeholder="选择要评价的商品" style="width:100%">
                  <el-option v-for="item in unreviewedItems(reviewManageOrder)" :key="item.furnitureId"
                             :label="item.furnitureName" :value="item.furnitureId"/>
                </el-select>
              </el-form-item>
              <el-form-item label="评分">
                <el-rate v-model="reviewManageForm.rating" :max="5" show-score/>
              </el-form-item>
              <el-form-item label="评价内容">
                <el-input v-model="reviewManageForm.content" type="textarea" :rows="3"
                          placeholder="分享你的使用体验..." maxlength="500" show-word-limit/>
              </el-form-item>
              <el-button type="primary" @click="submitManageReview" :loading="reviewManageSubmitting">提交追评
              </el-button>
            </el-form>
          </div>
          <el-empty v-if="existingReviews.length === 0 && unreviewedItems(reviewManageOrder).length === 0"
                    description="暂无评价数据"/>
        </div>
      </el-dialog>

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
                      <img :src="imgUrl(item.furnitureIcon, '/images/default-furniture.png')"
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
import {ref, reactive, onMounted} from 'vue'
import { useRouter } from 'vue-router'
import {ArrowLeft, User, Location, Check, Star, Delete} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {imgUrl} from '@/utils/img.js'
import { getUserOrders, cancelOrder as apiCancelOrder, confirmReceipt as apiConfirmReceipt } from '@/api/order.js'
import { getFurnitureById } from '@/api/furniture.js'
import {addReview, getOrderReviews, deleteReview} from '@/api/review.js'


const router = useRouter()

const loading = ref(true)
const orderList = ref([])
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)

const detailDialogVisible = ref(false)
const currentOrder = ref(null)

const reviewDialogVisible = ref(false)
const reviewTarget = ref(null)
const reviewForm = ref({furnitureId: null, rating: 5, content: ''})
const reviewSubmitting = ref(false)
const reviewedMap = reactive({})

const reviewManageVisible = ref(false)
const reviewManageOrder = ref(null)
const reviewManageForm = ref({furnitureId: null, rating: 5, content: ''})
const reviewManageSubmitting = ref(false)
const existingReviews = ref([])

const canReviewOrder = (order) => {
  if (order.status !== 3) return false
  const items = order.itemList || []
  if (items.length === 0) return false
  const reviewed = reviewedMap[order.id] || new Set()
  return items.some(item => !reviewed.has(item.furnitureId))
}

const reviewBtnText = (order) => {
  const reviewed = reviewedMap[order.id]
  return reviewed && reviewed.size > 0 ? '继续评价' : '去评价'
}

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
      4: '已取消',
      5: '已评价'
    }
    return map[status] || '未知状态'
}

const getStatusType = (status) => {
    const map = {
        0: 'warning',
        1: 'success',
        2: 'primary',
        3: 'info',
      4: 'danger',
      5: 'success'
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

const unreviewedItems = (order) => {
  const reviewed = reviewedMap[order.id] || new Set()
  return (order.itemList || []).filter(item => !reviewed.has(item.furnitureId))
}

const openReviewDialog = (order) => {
  reviewTarget.value = order
  const items = unreviewedItems(order)
  reviewForm.value = {
    furnitureId: items.length > 0 ? items[0].furnitureId : null,
    rating: 5,
    content: ''
  }
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  if (!reviewForm.value.furnitureId) {
    ElMessage.warning('请选择要评价的商品')
    return
  }
  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  reviewSubmitting.value = true
  try {
    const res = await addReview({
      orderId: reviewTarget.value.id,
      furnitureId: reviewForm.value.furnitureId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content.trim()
    })
    if (res.success || res.code === 200) {
      ElMessage.success('评价成功')
      const orderId = reviewTarget.value.id
      const fid = reviewForm.value.furnitureId
      if (!reviewedMap[orderId]) reviewedMap[orderId] = new Set()
      reviewedMap[orderId].add(fid)
      reviewDialogVisible.value = false
      loadOrders()
    } else {
      ElMessage.error(res.message || '评价失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '评价失败')
  } finally {
    reviewSubmitting.value = false
  }
}

const openReviewManageDialog = async (order) => {
  reviewManageOrder.value = order
  const items = unreviewedItems(order)
  reviewManageForm.value = {
    furnitureId: items.length > 0 ? items[0].furnitureId : null,
    rating: 5,
    content: ''
  }
  reviewManageVisible.value = true
  try {
    const res = await getOrderReviews(order.id)
    if ((res.success || res.code === 200) && Array.isArray(res.data)) {
      existingReviews.value = res.data
      if (!reviewedMap[order.id]) reviewedMap[order.id] = new Set()
      res.data.forEach(r => reviewedMap[order.id].add(r.furnitureId))
    }
  } catch (e) {
    existingReviews.value = []
  }
}

const handleDeleteReview = async (reviewId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteReview(reviewId)
    if (res.success || res.code === 200) {
      ElMessage.success('已删除')
      const deleted = existingReviews.value.find(r => r.id === reviewId)
      if (deleted && reviewManageOrder.value) {
        reviewedMap[reviewManageOrder.value.id]?.delete(deleted.furnitureId)
      }
      existingReviews.value = existingReviews.value.filter(r => r.id !== reviewId)
      loadOrders()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const submitManageReview = async () => {
  if (!reviewManageForm.value.furnitureId) {
    ElMessage.warning('请选择要评价的商品')
    return
  }
  if (!reviewManageForm.value.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  reviewManageSubmitting.value = true
  try {
    const res = await addReview({
      orderId: reviewManageOrder.value.id,
      furnitureId: reviewManageForm.value.furnitureId,
      rating: reviewManageForm.value.rating,
      content: reviewManageForm.value.content.trim()
    })
    if (res.success || res.code === 200) {
      ElMessage.success('评价成功')
      const oid = reviewManageOrder.value.id
      const fid = reviewManageForm.value.furnitureId
      if (!reviewedMap[oid]) reviewedMap[oid] = new Set()
      reviewedMap[oid].add(fid)
      await openReviewManageDialog(reviewManageOrder.value)
      loadOrders()
    } else {
      ElMessage.error(res.message || '评价失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '评价失败')
  } finally {
    reviewManageSubmitting.value = false
  }
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
<template>
    <div class="furniture-container">
        <!-- 顶部导航 -->
        <header class="header">
            <div class="header-content">
                <div class="logo" @click="goHome">
                    <span>🏠</span>
                    <h1>家具商城</h1>
                </div>
                <div class="nav-title">
                    <span class="back-btn" @click="goBack">← 返回</span>
                    <span class="divider">|</span>
                    <span class="current-title">{{ furniture.fName || '家具详情' }}</span>
                </div>
                <div class="user-info">
                  <div class="user-profile" @click="goToProfile">
                    <img :src="userIcon" class="user-avatar" alt="头像" @error="handleImageError"/>
                    <span class="welcome">{{ userName }}</span>
                  </div>
                </div>
            </div>
        </header>

        <!-- 主体内容 -->
        <main class="main-content" v-if="loading">
            <div class="loading-state">
                <div class="spinner"></div>
                <p>正在加载家具信息...</p>
            </div>
        </main>

        <main class="main-content" v-else-if="!furniture.id">
            <div class="empty-state">
                <div class="empty-icon">📦</div>
                <p>家具不存在或已下架</p>
                <button class="back-btn-large" @click="goBack">返回列表</button>
            </div>
        </main>

        <main class="main-content" v-else>
            <div class="detail-layout">
                <!-- 左侧：图片区域 -->
                <div class="image-section">
                    <div class="main-image">
                        <div class="image-placeholder-large">
                          <img :src="imgUrl(currentImage)"
                                :alt="furniture.fName" class="furniture-img-real" @error="handleImgError($event)" />
                        </div>
                        <div class="stock-tag" :class="{ 'low-stock': furniture.stock < 10 }">
                            库存 {{ furniture.stock }}
                        </div>
                    </div>
                  <div class="thumbnail-list" v-if="allImages.length > 1">
                    <img v-for="(img, idx) in allImages" :key="idx"
                         :src="imgUrl(img)" class="thumbnail"
                         :class="{ active: currentImage === img }"
                         @click="currentImage = img" @error="handleThumbError"/>
                  </div>
                </div>

                <!-- 右侧：信息区域 -->
                <div class="info-section">
                    <div class="info-header">
                        <h1 class="furniture-name">{{ furniture.fName }}</h1>
                        <p class="furniture-brand" v-if="furniture.brand">
                            <span>🏷️</span> {{ furniture.brand }}
                        </p>
                    </div>

                    <div class="price-section">
                        <span class="price-label">售价</span>
                        <span class="price-value">¥{{ formatPrice(furniture.price) }}</span>
                    </div>

                    <div class="intro-section" v-if="furniture.intro">
                        <h3>产品介绍</h3>
                        <p class="intro-text">{{ furniture.intro }}</p>
                    </div>

                    <div class="action-section">
                        <div class="quantity-selector">
                            <span class="label">数量</span>
                            <div class="quantity-control">
                                <button class="qty-btn" @click="decreaseQty" :disabled="quantity <= 1">-</button>
                                <span class="qty-value">{{ quantity }}</span>
                                <button class="qty-btn" @click="increaseQty"
                                    :disabled="quantity >= furniture.stock">+</button>
                            </div>
                        </div>
                        <div class="action-buttons">
                            <button class="btn-cart" @click="addToCart">
                                <span>🛒</span> 加入购物车
                            </button>
                            <button class="btn-buy" @click="buyNow" :disabled="furniture.stock <= 0">
                                <span>⚡</span> 立即购买
                            </button>
                          <button class="btn-fav" :class="{ favorited: isFavorited }" @click="handleToggleFav">
                            <span>{{ isFavorited ? '❤️' : '🤍' }}</span> {{ isFavorited ? '已收藏' : '收藏' }}
                          </button>
                        </div>
                    </div>
                </div>
            </div>

          <!-- 评价区域 -->
          <div class="review-section" v-if="furniture.id">
            <div class="review-head">
              <h3>商品评价</h3>
              <div class="review-scorecard" v-if="reviewStats.reviewCount > 0">
                <span class="score-big">{{ reviewStats.avgRating }}</span>
                <div class="score-meta">
                  <span class="score-stars">{{ '⭐'.repeat(reviewRatingStars) }}</span>
                  <span class="score-count">共 {{ reviewStats.reviewCount }} 条评价</span>
                </div>
              </div>
            </div>

            <div class="review-body">
              <div class="review-list" v-if="reviewList.length > 0">
                <div class="review-card" v-for="r in reviewList" :key="r.id">
                  <div class="review-card-hd">
                    <span class="review-user">{{ r.user_name || '匿名用户' }}</span>
                    <span class="review-stars">{{ '⭐'.repeat(r.rating) }}</span>
                    <span class="review-time">{{ formatTime(r.create_time) }}</span>
                  </div>
                  <p class="review-text" v-if="r.content">{{ r.content }}</p>
                </div>
              </div>
              <div class="review-empty" v-else>
                <p>暂无评价，成为第一个评价的人吧</p>
              </div>

            </div>
            </div>
        </main>

        <!-- 购买对话框 -->
        <el-dialog v-model="buyDialogVisible" title="确认订单信息" width="500px" :close-on-click-modal="false"
            class="buy-dialog">
            <div class="order-summary">
                <div class="summary-item">
                  <img :src="imgUrl(furniture.fIcon)" class="summary-img"
                        @error="handleSummaryImgError" />
                    <div class="summary-info">
                        <p class="summary-name">{{ furniture.fName }}</p>
                        <p class="summary-price">¥{{ formatPrice(furniture.price) }} × {{ quantity }}</p>
                    </div>
                    <div class="summary-total">
                        ¥{{ formatPrice(furniture.price * quantity) }}
                    </div>
                </div>
            </div>

            <el-form :model="buyForm" label-position="top" class="buy-form">
              <el-form-item label="选择收货地址" v-if="savedAddresses.length > 0">
                <el-select v-model="selectedAddressId" placeholder="选择已保存的地址" clearable
                           @change="onAddressSelect" style="width: 100%">
                  <el-option v-for="addr in savedAddresses" :key="addr.id"
                             :label="addr.consignee + ' ' + addr.phone + ' ' + addr.address"
                             :value="addr.id">
                    <span>{{ addr.consignee }} — {{ addr.phone }}</span>
                    <span style="color:#999;font-size:12px;display:block">{{ addr.address }}</span>
                  </el-option>
                </el-select>
              </el-form-item>

              <el-form-item label="收货人姓名 *">
                    <el-input v-model="buyForm.consignee" placeholder="请输入收货人姓名" maxlength="20" show-word-limit />
                </el-form-item>

                <el-form-item label="联系电话 *">
                    <el-input v-model="buyForm.phone" placeholder="请输入联系电话" maxlength="20" />
                </el-form-item>

                <el-form-item label="收货地址 *">
                    <el-input v-model="buyForm.address" type="textarea" :rows="3" placeholder="请输入详细收货地址"
                        maxlength="200" show-word-limit />
                </el-form-item>

                <el-form-item label="订单备注">
                    <el-input v-model="buyForm.remark" type="textarea" :rows="2" placeholder="请输入订单备注（选填）"
                        maxlength="200" show-word-limit />
                </el-form-item>
            </el-form>

            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="closeBuyDialog" size="large">取消</el-button>
                    <el-button type="primary" @click="submitBuy" :loading="buyLoading" class="submit-btn" size="large">
                        提交订单
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
    <CartDrawer />
</template>

<script setup>
import {ref, onMounted, computed, watch} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useFurnitureDetail } from '@/composables/useFurniture.js'
import {imgUrl} from '@/utils/img.js'

import CartDrawer from '@/components/CartDrawer.vue'
import { useCartStore } from '@/stores/cart.js'
import {toggleFavorite, checkFavorite} from '@/api/favorite.js'
import {getAddressList} from '@/api/address.js'
import {getReviews} from '@/api/review.js'

const cartStore = useCartStore()

const route = useRoute()
const router = useRouter()
const furnitureId = ref(route.params.id)
const isFavorited = ref(false)
const savedAddresses = ref([])
const selectedAddressId = ref(null)
const currentImage = ref('')

const allImages = computed(() => {
  const list = []
  if (furniture.value?.fIcon) {
    list.push(furniture.value.fIcon)
  }
  if (furniture.value?.images) {
    const extras = furniture.value.images.split(',').map(s => s.trim()).filter(Boolean)
    list.push(...extras)
  }
  return list
})

const userName = ref('用户')
const userIcon = ref('/images/default-avatar.png')

const {
    furniture,
    loading,
    quantity,
    buyDialogVisible,
    buyLoading,
    buyForm,
    formatPrice,
    decreaseQty,
    increaseQty,
    addToCart,
    buyNow,
    closeBuyDialog,
    submitBuy,
    loadFurnitureDetail,
    goBack,
    goHome
} = useFurnitureDetail()

const loadAddresses = async () => {
  try {
    const res = await getAddressList()
    if ((res.success || res.code === 200) && Array.isArray(res.data)) {
      savedAddresses.value = res.data
    }
  } catch (e) { /* ignore */
  }
}

const onAddressSelect = (id) => {
  if (!id) return
  const addr = savedAddresses.value.find(a => a.id === id)
  if (addr) {
    buyForm.consignee = addr.consignee
    buyForm.phone = addr.phone
    buyForm.address = addr.address
  }
}

watch(allImages, (imgs) => {
  if (imgs.length > 0 && !currentImage.value) {
    currentImage.value = imgs[0]
  }
}, {immediate: true})

const reviewList = ref([])
const reviewStats = ref({avgRating: 0, reviewCount: 0})
const reviewRatingStars = computed(() => Math.round(Number(reviewStats.value.avgRating) || 0))

const loadReviews = async () => {
  try {
    const res = await getReviews(furnitureId.value)
    if ((res.success || res.code === 200) && res.data) {
      reviewList.value = res.data.reviews || []
      reviewStats.value = {
        avgRating: Number(res.data.stats?.avg_rating || 0).toFixed(1),
        reviewCount: res.data.stats?.review_count || 0
      }
    }
  } catch (e) { /* ignore */
  }
}

const handleToggleFav = async () => {
  try {
    const res = await toggleFavorite(furnitureId.value)
    if (res.success || res.code === 200) {
      isFavorited.value = res.data
      ElMessage.success(isFavorited.value ? '已收藏' : '已取消收藏')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(async () => {
    loadUserInfo()
    loadFurnitureDetail(furnitureId.value)
  loadAddresses()
  loadReviews()
  try {
    const res = await checkFavorite(furnitureId.value)
    if (res.success || res.code === 200) {
      isFavorited.value = res.data
    }
  } catch (e) { /* ignore */
  }
})

const loadUserInfo = () => {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
        try {
            const userInfo = JSON.parse(userInfoStr)
            userName.value = userInfo.userName || '用户'
          userIcon.value = imgUrl(userInfo.icon, '/images/default-avatar.png')
        } catch (e) {
            userName.value = localStorage.getItem('userName') || '用户'
            userIcon.value = localStorage.getItem('userIcon') || '/images/default-avatar.png'
        }
    }
}

const handleImageError = (e) => {
    e.target.src = '/images/default-avatar.png'
}

const handleImgError = (e) => {
    e.target.style.display = 'none'
    e.target.parentElement.innerHTML = '🪑'
}

const handleThumbError = (e) => {
  e.target.style.display = 'none'
}

const handleSummaryImgError = (e) => {
    e.target.style.display = 'none'
    e.target.parentElement.querySelector('.summary-info').style.marginLeft = '0'
}

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

const goToProfile = () => {
    router.push('/user/profile')
}
</script>
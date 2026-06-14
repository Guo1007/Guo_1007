<template>
    <div class="furniture-container">
        <!-- 顶部导航 -->
        <header class="header">
            <div class="header-content">
                <div class="logo" @click="goHome">
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
                          <img v-if="!mainImgError" :src="imgUrl(currentImage)"
                               :alt="furniture.fName" class="furniture-img-real" @error="handleImgError"/>
                          <span v-else class="img-fallback">🪑</span>
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
                          <span></span> {{ furniture.brand }}
                        </p>
                    </div>

                    <div class="price-section">
                        <span class="price-label">售价</span>
                      <span class="price-value">¥{{ formatPrice(displayPrice) }}</span>
                      <span v-if="selectedSku" class="price-original">原价 ¥{{ formatPrice(furniture.price) }}</span>
                    </div>

                  <!-- 规格选择器 -->
                  <div class="spec-section" v-if="hasSpecs">
                    <div class="spec-group" v-for="group in specGroups" :key="group.id">
                      <div class="spec-group-label">{{ group.groupName }}</div>
                      <div class="spec-values">
                        <div v-for="val in group.values" :key="val.id"
                             class="spec-value-item"
                             :class="{
                                         active: selectedSpecs[group.groupName] === val.valueName,
                                         disabled: !isSpecValueAvailable(group.groupName, val.valueName)
                                     }"
                             @click="selectSpec(group.groupName, val.valueName)">
                          <img v-if="val.valueImage" :src="imgUrl(val.valueImage)" class="spec-value-img"/>
                          <span>{{ val.valueName }}</span>
                        </div>
                      </div>
                    </div>
                    <div class="spec-selected-info" v-if="selectedSku">
                      <span class="selected-label">已选：</span>
                      <span class="selected-text">{{ selectedSku.specText }}</span>
                    </div>
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
                                        :disabled="quantity >= displayStock">+
                                </button>
                            </div>
                          <span class="stock-hint" v-if="selectedSku">库存 {{ displayStock }} 件</span>
                        </div>
                        <div class="action-buttons">
                            <button class="btn-cart" @click="addToCart">
                              <span></span> 加入购物车
                            </button>
                          <button class="btn-buy" @click="buyNow" :disabled="displayStock <= 0">
                              <span></span> 立即购买
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
                  <img :src="imgUrl(displayImage || furniture.fIcon)" class="summary-img"
                        @error="handleSummaryImgError" />
                    <div class="summary-info">
                        <p class="summary-name">{{ furniture.fName }}</p>
                      <p class="summary-spec" v-if="selectedSku">{{ selectedSku.specText }}</p>
                      <p class="summary-price">¥{{ formatPrice(displayPrice) }} × {{ quantity }}</p>
                    </div>
                    <div class="summary-total">
                      ¥{{ formatPrice(displayPrice * quantity) }}
                    </div>
                </div>
            </div>

            <el-form :model="buyForm" label-position="top" class="buy-form">
              <el-form-item label="收货地址">
                <el-select v-model="selectedAddressId" placeholder="请选择收货地址"
                           @change="onAddressSelect" style="width: 100%">
                  <el-option v-for="addr in savedAddresses" :key="addr.id"
                             :label="addr.consignee + ' ' + addr.phone + ' ' + addr.address"
                             :value="addr.id">
                    <span>{{ addr.consignee }} — {{ addr.phone }}</span>
                    <span style="color:#999;font-size:12px;display:block">{{ addr.address }}</span>
                  </el-option>
                  <el-option :value="0" label="使用新地址">
                    <span style="color:#5a6a7a;">+ 使用新地址</span>
                  </el-option>
                </el-select>
                <div v-if="savedAddresses.length === 0" class="form-tip">
                  <el-text type="info" size="small">暂无已保存地址，请先
                    <el-link type="primary" @click="goToAddresses">添加地址</el-link>
                  </el-text>
                </div>
              </el-form-item>

              <template v-if="useNewAddress">
                <el-form-item label="收货人姓名 *">
                    <el-input v-model="buyForm.consignee" placeholder="请输入收货人姓名" maxlength="20" show-word-limit />
                </el-form-item>

                <el-form-item label="联系电话 *">
                    <el-input v-model="buyForm.phone" placeholder="请输入联系电话" maxlength="20" />
                </el-form-item>

                <el-form-item label="详细地址 *">
                  <el-input v-model="buyForm.address" type="textarea" :rows="3" placeholder="省/市/区 + 街道门牌号"
                        maxlength="200" show-word-limit />
                </el-form-item>
              </template>

              <el-form-item label="订单备注">
                    <el-input v-model="buyForm.remark" type="textarea" :rows="2" placeholder="请输入订单备注（选填）"
                        maxlength="200" show-word-limit />
                </el-form-item>
            </el-form>

            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="closeBuyDialog" size="large">取消</el-button>
                  <el-button type="primary" @click="handleSubmitBuy" :loading="buyLoading" class="submit-btn"
                             size="large">
                        提交订单
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
    <CartDrawer />
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {useFurnitureDetail} from '@/composables/useFurniture.js'
import {imgUrl} from '@/utils/img.js'

import CartDrawer from '@/components/CartDrawer.vue'
import {useCartStore} from '@/stores/cart.js'
import {checkFavorite, toggleFavorite} from '@/api/favorite.js'
import {getAddressList, saveAddress} from '@/api/address.js'
import {getReviews} from '@/api/review.js'

const cartStore = useCartStore()

const route = useRoute()
const router = useRouter()
const furnitureId = ref(route.params.id)
const isFavorited = ref(false)
const savedAddresses = ref([])
const selectedAddressId = ref(null)
const useNewAddress = ref(false)
const currentImage = ref('')
const mainImgError = ref(false)

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
  goHome,
  specGroups,
  skuList,
  selectedSpecs,
  selectedSku,
  hasSpecs,
  displayPrice,
  displayStock,
  displayImage,
  loadSpecs,
  selectSpec,
  isSpecValueAvailable
} = useFurnitureDetail()

const loadAddresses = async () => {
  try {
    const res = await getAddressList()
    if ((res.success || res.code === 200) && Array.isArray(res.data)) {
      savedAddresses.value = res.data
      // 如果有地址，默认选中默认地址或第一个
      if (savedAddresses.value.length > 0) {
        const defaultAddr = savedAddresses.value.find(a => a.isDefault === 1) || savedAddresses.value[0]
        selectedAddressId.value = defaultAddr.id
        buyForm.consignee = defaultAddr.consignee
        buyForm.phone = defaultAddr.phone
        buyForm.address = defaultAddr.address
        useNewAddress.value = false
      } else {
        useNewAddress.value = true
      }
    }
  } catch (e) { /* ignore */
  }
}

const onAddressSelect = (id) => {
  if (id === 0) {
    // 选择"使用新地址"
    useNewAddress.value = true
    buyForm.consignee = ''
    buyForm.phone = ''
    buyForm.address = ''
    return
  }
  if (!id) return
  const addr = savedAddresses.value.find(a => a.id === id)
  if (addr) {
    useNewAddress.value = false
    buyForm.consignee = addr.consignee
    buyForm.phone = addr.phone
    buyForm.address = addr.address
  }
}

const goToAddresses = () => {
  router.push('/user/addresses')
}

const handleSubmitBuy = async () => {
  const success = await submitBuy()
  if (success) {
    // 订单创建成功后，自动保存地址（静默处理，不打扰用户）
    try {
      await saveAddress({
        consignee: buyForm.consignee,
        phone: buyForm.phone,
        address: buyForm.address,
        isDefault: 0
      })
    } catch (e) {
      // 地址保存失败不影响主流程
      console.error('保存地址失败:', e)
    }
  }
}

watch(allImages, (imgs) => {
  if (imgs.length > 0 && !currentImage.value) {
    currentImage.value = imgs[0]
  }
}, {immediate: true})

watch(currentImage, () => {
  mainImgError.value = false
})

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
  loadSpecs(furnitureId.value)
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

const handleImgError = () => {
  mainImgError.value = true
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

<style scoped>
.form-tip {
  margin-top: 4px;
}

/* 规格选择器 */
.spec-section {
  margin: 20px 0;
  padding: 16px 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
}

.spec-group {
  margin-bottom: 16px;
}

.spec-group:last-child {
  margin-bottom: 8px;
}

.spec-group-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  font-weight: 500;
}

.spec-values {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.spec-value-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: all 0.2s;
  background: #fff;
}

.spec-value-item:hover:not(.disabled) {
  border-color: #3e4e49;
  color: #3e4e49;
}

.spec-value-item.active {
  border-color: #3e4e49;
  background: #f0f5f3;
  color: #3e4e49;
  font-weight: 500;
}

.spec-value-item.disabled {
  border-color: #f0f0f0;
  color: #ccc;
  cursor: not-allowed;
  background: #fafafa;
}

.spec-value-img {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  object-fit: cover;
}

.spec-selected-info {
  margin-top: 12px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  font-size: 13px;
}

.selected-label {
  color: #999;
}

.selected-text {
  color: #3e4e49;
  font-weight: 500;
}

.price-original {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  margin-left: 8px;
}

.stock-hint {
  font-size: 13px;
  color: #999;
  margin-left: 12px;
}

.summary-spec {
  font-size: 12px;
  color: #3e4e49;
  margin: 4px 0;
}
</style>
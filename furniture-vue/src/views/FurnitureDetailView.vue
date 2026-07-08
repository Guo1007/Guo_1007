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
            <div class="stock-tag" :class="{ 'low-stock': displayStock < 10 }">
              库存 {{ displayStock }}
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
            <div class="review-card" :id="'review-' + r.id" v-for="r in reviewList.slice(0, 2)" :key="r.id">
              <div class="review-card-hd">
                <img v-if="r.userAvatar" :src="imgUrl(r.userAvatar)" class="review-avatar"
                     @error="e => e.target.style.display='none'"/>
                <span v-else class="review-avatar-placeholder">👤</span>
                <span class="review-user">{{
                    (r.isAnonym && r.userId !== currentUserId) ? '匿名用户' : (r.userName || '用户')
                  }}</span>
                <span class="review-stars">{{ '⭐'.repeat(r.score) }}</span>
                <el-tag v-if="r.status === 0" type="warning" size="small">审核中</el-tag>
                <el-tag v-if="r.status === 2" type="danger" size="small">已删除</el-tag>
                <span class="review-time">{{ formatTime(r.createTime) }}</span>
                <el-button v-if="r.userId === currentUserId" text type="danger" size="small"
                           @click="handleDeleteReview(r.id)" style="margin-left:8px">删除
                </el-button>
              </div>
              <p class="review-text" v-if="r.content">{{ r.content }}</p>
              <div class="review-media" v-if="r.imgUrl || r.videoUrl">
                <img v-for="(img, idx) in parseImages(r.imgUrl)" :key="idx" :src="imgUrl(img)"
                     class="review-img" @click="previewImage(img)" @error="e => e.target.style.display='none'"/>
                <video v-if="r.videoUrl" :src="imgUrl(r.videoUrl)" controls class="review-video"
                       @click="previewVideo(imgUrl(r.videoUrl))"></video>
              </div>
              <!-- 追评 -->
              <div class="review-append" v-for="a in r.appendList" :key="a.id">
                <div class="append-hd">
                  <span class="append-tag">追评{{ a.appendNum === 1 ? '' : a.appendNum }}</span>
                  <el-tag v-if="a.status === 0" type="warning" size="small">审核中</el-tag>
                  <el-tag v-if="a.status === 2" type="danger" size="small">已删除</el-tag>
                  <el-button v-if="a.userId === currentUserId" text type="danger" size="small"
                             @click="handleDeleteAppend(a.id, r.id)" style="margin-left:auto">删除
                  </el-button>
                </div>
                <p class="append-text">{{ a.appendContent }}</p>
                <div class="review-media" v-if="a.appendImg">
                  <img v-for="(img, idx) in parseAppendImages(a.appendImg)" :key="'a'+idx" :src="imgUrl(img)"
                       class="review-img" @click="previewImage(img)" @error="e => e.target.style.display='none'"/>
                </div>
                <span class="append-time">{{ formatTime(a.appendTime) }}</span>
              </div>

              <!-- 评论区 -->
              <div class="review-comment-section">
                <div class="comment-toggle" @click="toggleComments(r.id)">
                  <el-icon>
                    <ChatLineSquare/>
                  </el-icon>
                  <span>评论 ({{ reviewCommentCountMap[r.id] || 0 }})</span>
                  <el-icon class="toggle-arrow" :class="{'is-expanded': expandedReviews[r.id]}">
                    <ArrowDown/>
                  </el-icon>
                </div>
                <div v-if="expandedReviews[r.id]" class="comment-panel">
                  <!-- 评论列表 -->
                  <div class="comment-list" v-if="reviewCommentsMap[r.id]?.length > 0">
                    <div v-for="c in reviewCommentsMap[r.id]" :key="c.id" class="comment-item">
                      <img v-if="c.userAvatar" :src="imgUrl(c.userAvatar)" class="comment-avatar"
                           @error="e => e.target.style.display='none'"/>
                      <span v-else class="comment-avatar-placeholder">👤</span>
                      <div class="comment-body">
                        <div class="comment-hd">
                          <span class="comment-user">{{ c.userName || '用户' }}</span>
                          <span v-if="c.replyToUserName" class="comment-reply-to">
                            回复 <span class="reply-user">{{ c.replyToUserName }}</span>
                          </span>
                          <el-tag v-if="c.status === 0" type="warning" size="small">审核中</el-tag>
                          <el-tag v-if="c.status === 2" type="danger" size="small">已删除</el-tag>
                          <span class="comment-time">{{ formatTime(c.createTime) }}</span>
                        </div>
                        <p class="comment-content">{{ c.content }}</p>
                        <div class="comment-actions">
                          <el-button text size="small" @click="replyToComment(r, c)">回复</el-button>
                          <el-button v-if="c.userId === currentUserId" text size="small" type="danger"
                                     @click="handleDeleteReviewComment(c.id, r.id)">删除
                          </el-button>
                        </div>
                        <!-- 子回复 -->
                        <div v-if="c.children?.length > 0" class="comment-children">
                          <div v-for="child in c.children" :key="child.id" class="comment-child-item">
                            <img v-if="child.userAvatar" :src="imgUrl(child.userAvatar)" class="comment-avatar-small"
                                 @error="e => e.target.style.display='none'"/>
                            <span v-else class="comment-avatar-placeholder-small">👤</span>
                            <div class="comment-child-body">
                              <div class="comment-hd">
                                <span class="comment-user">{{ child.userName || '用户' }}</span>
                                <span v-if="child.replyToUserName" class="comment-reply-to">
                                  回复 <span class="reply-user">{{ child.replyToUserName }}</span>
                                </span>
                                <el-tag v-if="child.status === 0" type="warning" size="small">审核中</el-tag>
                                <el-tag v-if="child.status === 2" type="danger" size="small">已删除</el-tag>
                                <span class="comment-time">{{ formatTime(child.createTime) }}</span>
                              </div>
                              <p class="comment-content">{{ child.content }}</p>
                              <div class="comment-actions">
                                <el-button text size="small" @click="replyToComment(r, child)">回复</el-button>
                                <el-button v-if="child.userId === currentUserId" text size="small" type="danger"
                                           @click="handleDeleteReviewComment(child.id, r.id)">删除
                                </el-button>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!-- 评论输入框 -->
                  <div class="comment-input-wrapper">
                    <el-input v-model="commentInputMap[r.id]" :placeholder="commentPlaceholderMap[r.id] || '写评论...'"
                              size="small" @keyup.enter="submitReviewComment(r.id, r.userId)">
                      <template #append>
                        <el-button @click="submitReviewComment(r.id, r.userId)" :loading="commentSubmittingMap[r.id]">
                          发送
                        </el-button>
                      </template>
                    </el-input>
                    <el-button v-if="commentReplyToMap[r.id]" text size="small" @click="cancelReply(r.id)">
                      取消回复
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="review-empty" v-else>
            <p>暂无评价，成为第一个评价的人吧</p>
          </div>
          <div class="review-more" v-if="reviewList.length > 2">
            <el-button text type="primary" @click="showAllReviews">查看全部 {{ reviewList.length }} 条评价</el-button>
          </div>
        </div>
      </div>
    </main>

    <!-- 全部评价弹窗 -->
    <el-dialog v-model="reviewDialogVisible" title="全部评价" width="650px" :close-on-click-modal="true">
      <div class="review-dialog-content">
        <div class="review-dialog-stats" v-if="reviewStats.reviewCount > 0">
          <span class="score-big">{{ reviewStats.avgRating }}</span>
          <div class="score-meta">
            <span class="score-stars">{{ '⭐'.repeat(reviewRatingStars) }}</span>
            <span class="score-count">共 {{ reviewList.length }} 条评价</span>
          </div>
        </div>
        <el-divider/>
        <div class="review-dialog-list">
          <div class="review-card" :id="'review-dialog-' + r.id" v-for="r in reviewList" :key="r.id">
            <div class="review-card-hd">
              <img v-if="r.userAvatar" :src="imgUrl(r.userAvatar)" class="review-avatar"
                   @error="e => e.target.style.display='none'"/>
              <span v-else class="review-avatar-placeholder">👤</span>
              <span class="review-user">{{
                  (r.isAnonym && r.userId !== currentUserId) ? '匿名用户' : (r.userName || '用户')
                }}</span>
              <span class="review-stars">{{ '⭐'.repeat(r.score) }}</span>
              <el-tag v-if="r.status === 0" type="warning" size="small">审核中</el-tag>
              <el-tag v-if="r.status === 2" type="danger" size="small">已删除</el-tag>
              <span class="review-time">{{ formatTime(r.createTime) }}</span>
            </div>
            <p class="review-text" v-if="r.content">{{ r.content }}</p>
            <div class="review-media" v-if="r.imgUrl || r.videoUrl">
              <img v-for="(img, idx) in parseImages(r.imgUrl)" :key="idx" :src="imgUrl(img)"
                   class="review-img" @click="previewImage(img)" @error="e => e.target.style.display='none'"/>
              <video v-if="r.videoUrl" :src="imgUrl(r.videoUrl)" controls class="review-video"
                     @click="previewVideo(imgUrl(r.videoUrl))"></video>
            </div>
            <!-- 追评 -->
            <div class="review-append" v-for="a in r.appendList" :key="a.id">
              <div class="append-tag">追评{{ a.appendNum === 1 ? '' : a.appendNum }}</div>
              <p class="append-text">{{ a.appendContent }}</p>
              <div class="review-media" v-if="a.appendImg">
                <img v-for="(img, idx) in parseAppendImages(a.appendImg)" :key="'a'+idx" :src="imgUrl(img)"
                     class="review-img" @click="previewImage(img)" @error="e => e.target.style.display='none'"/>
              </div>
              <span class="append-time">{{ formatTime(a.appendTime) }}</span>
            </div>

            <!-- 评论区 -->
            <div class="review-comment-section">
              <div class="comment-toggle" @click="toggleComments(r.id)">
                <el-icon>
                  <ChatLineSquare/>
                </el-icon>
                <span>评论 ({{ reviewCommentCountMap[r.id] || 0 }})</span>
                <el-icon class="toggle-arrow" :class="{'is-expanded': expandedReviews[r.id]}">
                  <ArrowDown/>
                </el-icon>
              </div>
              <div v-if="expandedReviews[r.id]" class="comment-panel">
                <!-- 评论列表 -->
                <div class="comment-list" v-if="reviewCommentsMap[r.id]?.length > 0">
                  <div v-for="c in reviewCommentsMap[r.id]" :key="c.id" :id="'review-comment-' + c.id" class="comment-item">
                    <img v-if="c.userAvatar" :src="imgUrl(c.userAvatar)" class="comment-avatar"
                         @error="e => e.target.style.display='none'"/>
                    <span v-else class="comment-avatar-placeholder">👤</span>
                    <div class="comment-body">
                      <div class="comment-hd">
                        <span class="comment-user">{{ c.userName || '用户' }}</span>
                        <span v-if="c.replyToUserName" class="comment-reply-to">
                          回复 <span class="reply-user">{{ c.replyToUserName }}</span>
                        </span>
                        <span class="comment-time">{{ formatTime(c.createTime) }}</span>
                      </div>
                      <p class="comment-content">{{ c.content }}</p>
                      <div class="comment-actions">
                        <el-button text size="small" @click="replyToComment(r, c)">回复</el-button>
                        <el-button v-if="c.userId === currentUserId" text size="small" type="danger"
                                   @click="handleDeleteReviewComment(c.id, r.id)">删除
                        </el-button>
                      </div>
                      <!-- 子回复 -->
                      <div v-if="c.children?.length > 0" class="comment-children">
                        <div v-for="child in c.children" :key="child.id" :id="'review-comment-' + child.id" class="comment-child-item">
                          <img v-if="child.userAvatar" :src="imgUrl(child.userAvatar)" class="comment-avatar-small"
                               @error="e => e.target.style.display='none'"/>
                          <span v-else class="comment-avatar-placeholder-small">👤</span>
                          <div class="comment-child-body">
                            <div class="comment-hd">
                              <span class="comment-user">{{ child.userName || '用户' }}</span>
                              <span v-if="child.replyToUserName" class="comment-reply-to">
                                回复 <span class="reply-user">{{ child.replyToUserName }}</span>
                              </span>
                              <span class="comment-time">{{ formatTime(child.createTime) }}</span>
                            </div>
                            <p class="comment-content">{{ child.content }}</p>
                            <div class="comment-actions">
                              <el-button text size="small" @click="replyToComment(r, child)">回复</el-button>
                              <el-button v-if="child.userId === currentUserId" text size="small" type="danger"
                                         @click="handleDeleteReviewComment(child.id, r.id)">删除
                              </el-button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 评论输入框 -->
                <div class="comment-input-wrapper">
                  <el-input v-model="commentInputMap[r.id]" :placeholder="commentPlaceholderMap[r.id] || '写评论...'"
                            size="small" @keyup.enter="submitReviewComment(r.id, r.userId)">
                    <template #append>
                      <el-button @click="submitReviewComment(r.id, r.userId)" :loading="commentSubmittingMap[r.id]">
                        发送
                      </el-button>
                    </template>
                  </el-input>
                  <el-button v-if="commentReplyToMap[r.id]" text size="small" @click="cancelReply(r.id)">
                    取消回复
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 图片预览弹窗 -->
    <el-dialog v-model="imagePreviewVisible" title="图片预览" width="700px" :close-on-click-modal="true"
               class="image-preview-dialog">
      <div class="image-preview-body">
        <img :src="previewImageUrl" class="preview-img-full" @error="e => e.target.style.display='none'"/>
      </div>
    </el-dialog>

    <!-- 视频预览弹窗 -->
    <el-dialog v-model="videoPreviewVisible" title="视频预览" width="700px" :close-on-click-modal="true"
               class="video-preview-dialog">
      <div class="video-preview-body">
        <video :src="previewVideoUrl" controls class="preview-video-full"></video>
      </div>
    </el-dialog>

    <!-- 购买对话框 -->
    <el-dialog v-model="buyDialogVisible" title="确认订单信息" width="500px" :close-on-click-modal="false"
               class="buy-dialog">
      <div class="order-summary">
        <div class="summary-item">
          <img :src="imgUrl(displayImage || furniture.fIcon)" class="summary-img"
               @error="handleSummaryImgError"/>
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
            <el-input v-model="buyForm.consignee" placeholder="请输入收货人姓名" maxlength="20" show-word-limit/>
          </el-form-item>

          <el-form-item label="联系电话 *">
            <el-input v-model="buyForm.phone" placeholder="请输入联系电话" maxlength="20"/>
          </el-form-item>

          <el-form-item label="详细地址 *">
            <el-input v-model="buyForm.address" type="textarea" :rows="3" placeholder="省/市/区 + 街道门牌号"
                      maxlength="200" show-word-limit/>
          </el-form-item>
        </template>

        <el-form-item label="订单备注">
          <el-input v-model="buyForm.remark" type="textarea" :rows="2" placeholder="请输入订单备注（选填）"
                    maxlength="200" show-word-limit/>
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
</template>

<script setup>
import {computed, nextTick, onMounted, reactive, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {ArrowDown, ChatLineSquare} from '@element-plus/icons-vue'
import {useFurnitureDetail} from '@/composables/useFurniture.js'
import {imgUrl} from '@/utils/img.js'

import {useCartStore} from '@/stores/cart.js'
import {checkFavorite, toggleFavorite} from '@/api/favorite.js'
import {getAddressList, saveAddress} from '@/api/address.js'
import {deleteAppend, deleteReview, getComments} from '@/api/comment.js'
import {addReviewComment, deleteReviewComment, getReviewComments} from '@/api/reviewComment.js'

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
const currentUserId = ref(null)

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
  buyNow: originalBuyNow,
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

// 重写 buyNow，在打开对话框时填入已选地址
const buyNow = () => {
  if (hasSpecs.value && !selectedSku.value) {
    ElMessage.warning('请先选择规格')
    return
  }
  if (displayStock.value <= 0) {
    ElMessage.warning('该商品暂时缺货')
    return
  }
  console.log('buyNow 被调用, savedAddresses:', savedAddresses.value)
  // 先打开对话框
  buyDialogVisible.value = true
  // 如果有已保存的地址，填入默认地址
  if (savedAddresses.value.length > 0) {
    const defaultAddr = savedAddresses.value.find(a => a.isDefault === 1) || savedAddresses.value[0]
    console.log('选中地址:', defaultAddr)
    selectedAddressId.value = defaultAddr.id
    buyForm.value.consignee = defaultAddr.consignee
    buyForm.value.phone = defaultAddr.phone
    buyForm.value.address = defaultAddr.address
    useNewAddress.value = false
    console.log('buyForm 设置后:', buyForm.value)
  } else {
    // 没有地址，清空表单并显示输入框
    buyForm.value = {consignee: '', phone: '', address: '', remark: ''}
    selectedAddressId.value = null
    useNewAddress.value = true
  }
}

const loadAddresses = async () => {
  try {
    const res = await getAddressList()
    if ((res.success || res.code === 200) && Array.isArray(res.data)) {
      savedAddresses.value = res.data
      // 如果有地址，默认选中默认地址或第一个
      if (savedAddresses.value.length > 0) {
        const defaultAddr = savedAddresses.value.find(a => a.isDefault === 1) || savedAddresses.value[0]
        selectedAddressId.value = defaultAddr.id
        buyForm.value.consignee = defaultAddr.consignee
        buyForm.value.phone = defaultAddr.phone
        buyForm.value.address = defaultAddr.address
        useNewAddress.value = false
      } else {
        useNewAddress.value = true
      }
    }
  } catch (e) { /* ignore */
  }
}

const onAddressSelect = (id) => {
  console.log('onAddressSelect 被调用, id:', id)
  if (id === 0) {
    // 选择"使用新地址"
    useNewAddress.value = true
    buyForm.value = {consignee: '', phone: '', address: '', remark: ''}
    return
  }
  if (!id) return
  const addr = savedAddresses.value.find(a => a.id === id)
  if (addr) {
    useNewAddress.value = false
    // 使用展开运算符创建新对象，确保响应式更新
    buyForm.value = {
      consignee: addr.consignee,
      phone: addr.phone,
      address: addr.address,
      remark: ''
    }
    console.log('地址选择后 buyForm.value:', buyForm.value)
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
        consignee: buyForm.value.consignee,
        phone: buyForm.value.phone,
        address: buyForm.value.address,
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

// 选中SKU有独立图片时，自动切换到SKU图片
watch(selectedSku, (newSku) => {
  if (newSku && newSku.skuImage) {
    currentImage.value = newSku.skuImage
  } else if (allImages.value.length > 0) {
    currentImage.value = allImages.value[0]
  }
})

const reviewList = ref([])
const reviewStats = ref({avgRating: 0, reviewCount: 0})
const reviewRatingStars = computed(() => Math.round(Number(reviewStats.value.avgRating) || 0))

const loadReviews = async () => {
  try {
    const res = await getComments(furnitureId.value, 1, 100)
    if ((res.success || res.code === 200) && res.data) {
      const records = res.data.records || res.data || []
      reviewList.value = records
      // 计算平均分
      if (records.length > 0) {
        const totalScore = records.reduce((sum, c) => sum + (c.score || 0), 0)
        reviewStats.value = {
          avgRating: (totalScore / records.length).toFixed(1),
          reviewCount: records.length
        }
      }
      // 加载每条评价的评论数
      loadAllCommentCounts(records)
      // 如果 URL 带了 reviewId，滚动到对应位置
      const targetReviewId = route.query.reviewId
      const targetReviewCommentId = route.query.reviewCommentId
      if (targetReviewId) {
        await handleNotificationScroll(Number(targetReviewId), targetReviewCommentId ? Number(targetReviewCommentId) : null)
      }
    }
  } catch (e) { /* ignore */
  }
}

// 加载所有评价的评论数（不展开评论内容）
const loadAllCommentCounts = async (reviews) => {
  for (const r of reviews) {
    try {
      const res = await getReviewComments(r.id)
      if ((res.success || res.code === 200) && res.data) {
        reviewCommentCountMap[r.id] = countComments(res.data)
      }
    } catch (e) {
      reviewCommentCountMap[r.id] = 0
    }
  }
}

// 通知跳转：打开评价弹窗 → 展开评论区 → 滚动到具体回复并高亮
const handleNotificationScroll = (reviewId, reviewCommentId) => {
  return new Promise((resolve) => {
    // 1. 打开全部评价弹窗
    reviewDialogVisible.value = true
    // el-dialog 有 CSS 动画，setTimeout 等到动画结束后再操作 DOM
    setTimeout(async () => {
      try {
        // 2. 滚动到目标评价
        const reviewEl = document.getElementById('review-dialog-' + reviewId)
        if (!reviewEl) { resolve(); return }
        reviewEl.scrollIntoView({behavior: 'smooth', block: 'center'})

        if (reviewCommentId) {
          // 3. 展开评论区
          expandedReviews[reviewId] = true
          // 4. 加载评论数据
          if (!reviewCommentsMap[reviewId]) {
            await loadReviewComments(reviewId)
          }
          await nextTick()
          // 5. 滚动到具体评论并高亮
          const commentEl = document.getElementById('review-comment-' + reviewCommentId)
          if (commentEl) {
            commentEl.scrollIntoView({behavior: 'smooth', block: 'center'})
            commentEl.classList.add('review-highlight')
            setTimeout(() => commentEl.classList.remove('review-highlight'), 2000)
          }
        } else {
          // 没有评论ID，高亮评价本身
          reviewEl.classList.add('review-highlight')
          setTimeout(() => reviewEl.classList.remove('review-highlight'), 2000)
        }
      } catch (e) {
        console.error('handleNotificationScroll 出错:', e)
      }
      resolve()
    }, 350)  // 等 el-dialog 打开动画完成（默认 ~300ms）
  })
}

const reviewDialogVisible = ref(false)
const showAllReviews = () => {
  reviewDialogVisible.value = true
}

// 咨询功能
// 图片预览
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')
const previewImage = (url) => {
  previewImageUrl.value = url
  imagePreviewVisible.value = true
}

// 视频预览
const videoPreviewVisible = ref(false)
const previewVideoUrl = ref('')
const previewVideo = (url) => {
  previewVideoUrl.value = url
  videoPreviewVisible.value = true
}

const parseImages = (images) => {
  if (!images) return []
  try {
    const arr = JSON.parse(images)
    return Array.isArray(arr) ? arr : []
  } catch {
    return images.split(',').filter(img => img.trim())
  }
}
const parseAppendImages = parseImages

// ========== 评论区功能 ==========
const expandedReviews = reactive({})
const reviewCommentsMap = reactive({})
const reviewCommentCountMap = reactive({})
const commentInputMap = reactive({})
const commentPlaceholderMap = reactive({})
const commentReplyToMap = reactive({})
const commentSubmittingMap = reactive({})

const toggleComments = async (reviewId) => {
  expandedReviews[reviewId] = !expandedReviews[reviewId]
  // 展开时加载评论内容（如果还没加载）
  if (expandedReviews[reviewId] && !reviewCommentsMap[reviewId]) {
    await loadReviewComments(reviewId)
  }
}

const loadReviewComments = async (reviewId) => {
  try {
    const res = await getReviewComments(reviewId)
    if ((res.success || res.code === 200) && res.data) {
      reviewCommentsMap[reviewId] = res.data
      reviewCommentCountMap[reviewId] = countComments(res.data)
    }
  } catch (e) {
    reviewCommentsMap[reviewId] = []
    reviewCommentCountMap[reviewId] = 0
  }
}

const countComments = (list) => {
  let count = 0
  for (const c of list) {
    count++
    if (c.children?.length > 0) {
      count += countComments(c.children)
    }
  }
  return count
}

const replyToComment = (review, comment) => {
  console.log('replyToComment review:', review.id, 'comment:', comment)
  commentReplyToMap[review.id] = comment
  commentPlaceholderMap[review.id] = `回复 ${comment.userName || '用户'}：`
  commentInputMap[review.id] = ''
}

const cancelReply = (reviewId) => {
  commentReplyToMap[reviewId] = null
  commentPlaceholderMap[reviewId] = '写评论...'
  commentInputMap[reviewId] = ''
}

const submitReviewComment = async (reviewId, reviewUserId) => {
  const content = (commentInputMap[reviewId] || '').trim()
  if (!content) {
    ElMessage.warning('请输入评论内容')
    return
  }
  commentSubmittingMap[reviewId] = true
  try {
    const replyTo = commentReplyToMap[reviewId]
    // 回复评论时用评论者id，直接评论评价时用评价作者id
    const targetUserId = replyTo ? replyTo.userId : reviewUserId
    const data = {
      reviewId: reviewId,
      content: content,
      replyToUserId: targetUserId && targetUserId !== currentUserId.value ? targetUserId : null,
      replyToCommentId: replyTo ? (replyTo.replyToCommentId || replyTo.id) : null
    }
    const res = await addReviewComment(data)
    if (res.success || res.code === 200) {
      ElMessage.success('评论成功')
      commentInputMap[reviewId] = ''
      commentReplyToMap[reviewId] = null
      commentPlaceholderMap[reviewId] = '写评论...'
      await loadReviewComments(reviewId)
    } else {
      ElMessage.error(res.msg || '评论失败')
    }
  } catch (e) {
    console.error('评论失败:', e)
  } finally {
    commentSubmittingMap[reviewId] = false
  }
}

const handleDeleteReviewComment = async (commentId, reviewId) => {
  try {
    await ElMessageBox.confirm('确定删除该评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteReviewComment(commentId)
    if (res.success || res.code === 200) {
      ElMessage.success('删除成功')
      // 重新加载评论并更新计数
      const commentsRes = await getReviewComments(reviewId)
      if ((commentsRes.success || commentsRes.code === 200) && commentsRes.data) {
        reviewCommentsMap[reviewId] = commentsRes.data
        reviewCommentCountMap[reviewId] = countComments(commentsRes.data)
      }
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除失败:', e)
    }
  }
}

const handleDeleteReview = async (reviewId) => {
  try {
    await ElMessageBox.confirm('确定删除该评价吗？删除后不可恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteReview(reviewId)
    if (res.success || res.code === 200) {
      ElMessage.success('删除成功')
      loadReviews()
    }
  } catch (e) {
    if (e !== 'cancel') console.error('删除失败:', e)
  }
}

const handleDeleteAppend = async (appendId, reviewId) => {
  try {
    await ElMessageBox.confirm('确定删除该追评吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteAppend(appendId)
    if (res.success || res.code === 200) {
      ElMessage.success('删除成功')
      loadReviews()
    }
  } catch (e) {
    if (e !== 'cancel') console.error('删除失败:', e)
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
    console.error('操作失败:', e)
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
      currentUserId.value = userInfo.id || null
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

.review-card {
  padding: 20px 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.3s;
}

.review-highlight {
  background: #fff9e6 !important;
  border-radius: 8px;
}

.review-card:last-child {
  border-bottom: none;
}

.review-card-hd {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.review-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.review-avatar-placeholder {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.review-text {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin: 0;
  padding-left: 42px;
}

.review-media {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
  padding-left: 42px;
}

.review-img {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  object-fit: cover;
  cursor: pointer;
}

.review-video {
  width: 160px;
  height: 120px;
  border-radius: 6px;
  object-fit: cover;
  cursor: pointer;
}

.review-video:hover {
  opacity: 0.85;
  transition: opacity 0.2s;
}

.review-append {
  margin-top: 12px;
  padding: 12px 16px;
  margin-left: 42px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 3px solid #409eff;
}

.append-hd {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.append-tag {
  font-size: 12px;
  color: #409eff;
  font-weight: 500;
}

.append-text {
  font-size: 13px;
  color: #333;
  line-height: 1.5;
  margin: 0 0 6px 0;
}

.append-time {
  font-size: 12px;
  color: #999;
}

.review-more {
  text-align: center;
  padding: 16px 0;
}

.review-dialog-content {
  max-height: 500px;
  overflow-y: auto;
}

.review-dialog-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 0 8px 0;
}

.review-dialog-list .review-card {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.review-dialog-list .review-card:last-child {
  border-bottom: none;
}

.review-user {
  font-weight: 500;
  color: #333;
}

.review-stars {
  font-size: 14px;
}

.review-time {
  margin-left: auto;
  color: #999;
  font-size: 13px;
}

/* 图片预览 */
.image-preview-dialog :deep(.el-dialog__body) {
  padding: 12px 20px;
}

.image-preview-body {
  display: flex;
  justify-content: center;
  align-items: center;
}

.preview-img-full {
  max-width: 100%;
  max-height: 500px;
  border-radius: 8px;
  object-fit: contain;
}

.review-img:hover {
  opacity: 0.85;
  transform: scale(1.02);
  transition: all 0.2s;
}

/* 视频预览 */
.video-preview-dialog :deep(.el-dialog__body) {
  padding: 12px 20px;
}

.video-preview-body {
  display: flex;
  justify-content: center;
  align-items: center;
}

.preview-video-full {
  max-width: 100%;
  max-height: 500px;
  border-radius: 8px;
}

/* ========== 评论区样式 ========== */
.review-comment-section {
  margin-top: 12px;
  padding-left: 42px;
}

.comment-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  padding: 6px 0;
  user-select: none;
}

.comment-toggle:hover {
  color: #409eff;
}

.toggle-arrow {
  transition: transform 0.2s;
  font-size: 12px;
}

.toggle-arrow.is-expanded {
  transform: rotate(180deg);
}

.comment-panel {
  margin-top: 8px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  display: flex;
  gap: 10px;
}

.comment-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.comment-avatar-placeholder {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-hd {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.comment-user {
  font-size: 13px;
  font-weight: 500;
  color: #333;
}

.comment-reply-to {
  font-size: 12px;
  color: #999;
}

.reply-user {
  color: #409eff;
}

.comment-time {
  font-size: 11px;
  color: #999;
  margin-left: auto;
}

.comment-content {
  font-size: 13px;
  color: #333;
  line-height: 1.5;
  margin: 4px 0;
}

.comment-actions {
  display: flex;
  gap: 4px;
}

/* 子评论 */
.comment-children {
  margin-top: 10px;
  padding: 10px;
  background: #fff;
  border-radius: 6px;
  border-left: 2px solid #e8e8e8;
}

.comment-child-item {
  display: flex;
  gap: 8px;
  padding: 6px 0;
}

.comment-child-item + .comment-child-item {
  border-top: 1px solid #f0f0f0;
}

.comment-avatar-small {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.comment-avatar-placeholder-small {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  flex-shrink: 0;
}

.comment-child-body {
  flex: 1;
  min-width: 0;
}

/* 评论输入框 */
.comment-input-wrapper {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e8e8e8;
}
</style>
<template>
  <div class="notification-bell" ref="bellRef">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="badge" :max="99">
      <el-button class="bell-btn" @click="toggleDropdown">
        <el-icon :size="22">
          <Bell/>
        </el-icon>
        <span class="bell-text" v-if="unreadCount > 0">{{ unreadCount }}</span>
      </el-button>
    </el-badge>

    <transition name="el-zoom-in-top">
      <div v-if="showDropdown" class="notification-dropdown">
        <div class="dropdown-header">
          <span class="title">消息通知</span>
          <div class="header-actions">
            <el-button v-if="unreadCount > 0" type="primary" size="small" plain @click="handleMarkAllRead">
              全部已读
            </el-button>
          </div>
        </div>
        <div class="dropdown-body">
          <div v-if="loading" class="loading">
            <el-icon class="loading-icon" :size="18">
              <Loading/>
            </el-icon>
            加载中...
          </div>
          <div v-else-if="list.length === 0" class="empty">暂无通知</div>
          <div v-for="item in list" :key="item.id" class="notif-item"
               :class="{ unread: !item.isRead }" @click="handleClick(item)">
            <div class="notif-dot" v-if="!item.isRead"></div>
            <div class="notif-type-icon">
              {{ typeIcon(item.type) }}
            </div>
            <div class="notif-content">
              <div class="notif-title">{{ item.title }}</div>
              <div class="notif-desc">{{ item.content }}</div>
              <div class="notif-meta">
                <span class="notif-time">{{ formatTime(item.createTime) }}</span>
                <el-tag v-if="!item.isRead" size="small" type="danger" effect="plain">新</el-tag>
              </div>
            </div>
            <el-button class="delete-btn" size="small" text type="danger" @click.stop="handleDelete(item)">
              <el-icon :size="14"><Delete/></el-icon>
            </el-button>
          </div>
        </div>
        <div class="dropdown-footer">
          <el-button type="text" size="small" @click="goToAll">查看全部通知</el-button>
        </div>
      </div>
    </transition>

    <!-- 通知详情弹窗 -->
    <el-dialog v-model="detailVisible" title="通知详情" width="520px" :close-on-click-modal="true">
      <div class="detail-container" v-if="detailItem">
        <div class="detail-header">
          <el-tag :type="detailItem.type === 'system' ? '' : detailItem.type === 'order' ? 'warning' : 'success'"
                  size="small" effect="plain">
            {{ typeLabel(detailItem.type) }}
          </el-tag>
          <span class="detail-time">{{ formatTime(detailItem.createTime) }}</span>
        </div>
        <h2 class="detail-title">{{ detailItem.title }}</h2>
        <el-divider/>
        <div class="detail-content">{{ detailItem.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, onUnmounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {Bell, Loading, Delete} from '@element-plus/icons-vue'
import {getNotificationList, getUnreadCount, markAllAsRead, markAsRead, deleteMyNotification} from '@/api/notification.js'
import {ElMessage, ElMessageBox} from 'element-plus'

const router = useRouter()
const bellRef = ref(null)
const showDropdown = ref(false)
const unreadCount = ref(0)
const list = ref([])
const total = ref(0)
const loading = ref(false)

// 详情弹窗
const detailVisible = ref(false)
const detailItem = ref(null)

let timer = null

const onClickOutside = (e) => {
  if (bellRef.value && !bellRef.value.contains(e.target)) {
    showDropdown.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    if (res.success) {
      unreadCount.value = res.data || 0
    }
  } catch (e) {
    // ignore
  }
}

const loadRecent = async () => {
  loading.value = true
  try {
    const res = await getNotificationList(1, 5)
    if (res.success && res.data) {
      list.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
  if (showDropdown.value) {
    loadRecent()
  }
}

const handleClick = async (item) => {
  if (!item.isRead) {
    await markAsRead(item.id)
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    item.isRead = true
  }
  // 评论回复通知：跳转到对应商品详情页
  if (item.type === 'comment_reply' && item.goodsId) {
    showDropdown.value = false
    router.push({path: `/furniture/detail/${item.goodsId}`, query: {reviewId: item.reviewId, reviewCommentId: item.reviewCommentId}})
    return
  }
  // 打开详情弹窗
  detailItem.value = item
  detailVisible.value = true
  showDropdown.value = false
}

const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    list.value.forEach(item => item.isRead = true)
    unreadCount.value = 0
  } catch (e) {
    // ignore
  }
}

const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteMyNotification(item.id)
    list.value = list.value.filter(n => n.id !== item.id)
    if (!item.isRead) {
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
    ElMessage.success('已删除')
  } catch (e) {
    console.error('删除通知失败:', e)
  }
}

const goToAll = () => {
  showDropdown.value = false
  router.push('/notification')
}

const typeIcon = (type) => {
  const map = {system: '📢', order: '📦', promotion: '🏷️', comment_reply: '💬'}
  return map[type] || '📢'
}

const typeLabel = (type) => {
  const map = {system: '系统通知', order: '订单通知', promotion: '促销通知', comment_reply: '回复通知'}
  return map[type] || '通知'
}

const formatTime = (t) => {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  loadUnreadCount()
  timer = setInterval(loadUnreadCount, 30000)
  document.addEventListener('click', onClickOutside)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  document.removeEventListener('click', onClickOutside)
})
</script>

<style scoped>
.notification-bell {
  position: relative;
  display: inline-block;
}

.badge {
  :deep(.el-badge__content) {
    font-size: 11px;
    height: 18px;
    line-height: 18px;
    padding: 0 6px;
  }
}

.bell-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  padding: 0;
  border: none;
  border-radius: 10px;
  background: #f5f5f5;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  color: #555;

  &:hover {
    background: #e8eaec;
    color: #333;
  }
}

.bell-text {
  display: none;
}

.notification-dropdown {
  position: absolute;
  right: 0;
  top: 48px;
  width: 360px;
  max-height: 480px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  z-index: 2000;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.dropdown-header .title {
  font-weight: 600;
  font-size: 15px;
  color: #333;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dropdown-body {
  flex: 1;
  overflow-y: auto;
  max-height: 360px;
}

.loading,
.empty {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.loading-icon {
  animation: rotating 1.5s linear infinite;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 20px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f8f8f8;
}

.notif-item .delete-btn {
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.2s;
  padding: 2px;
}

.notif-item:hover .delete-btn {
  opacity: 1;
}

.notif-item:hover {
  background: #f5f7fa;
}

.notif-item.unread {
  background: #f7f8fa;
}

.notif-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #5a6a7a;
  margin-top: 6px;
  flex-shrink: 0;
}

.notif-type-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  background: #f0f2f5;
  flex-shrink: 0;
}

.notif-content {
  flex: 1;
  min-width: 0;
}

.notif-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  line-height: 1.3;
}

.notif-desc {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
  line-height: 1.4;
}

.notif-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notif-time {
  font-size: 12px;
  color: #ccc;
}

.dropdown-footer {
  text-align: center;
  padding: 12px;
  border-top: 1px solid #f0f0f0;
}

/* 详情弹窗样式 */
.detail-container {
  padding: 0 4px;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.detail-time {
  font-size: 13px;
  color: #999;
}

.detail-title {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.detail-content {
  font-size: 15px;
  color: #444;
  line-height: 1.8;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>

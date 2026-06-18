<template>
  <div class="notification-page">
    <div class="top-nav">
      <div class="nav-content">
        <el-button text @click="goBack" class="back-btn">
          <el-icon>
            <ArrowLeft/>
          </el-icon>
          返回
        </el-button>
        <div class="breadcrumb">首页 / 消息通知</div>
        <el-button v-if="unreadCount > 0" type="primary" size="small" @click="handleMarkAllRead">
          全部已读
        </el-button>
      </div>
    </div>

    <div class="notif-container">
      <el-tabs v-model="activeTab" @tab-change="loadData">
        <el-tab-pane label="全部通知" name="all"></el-tab-pane>
      </el-tabs>

      <div v-if="loading" class="loading">加载中...</div>

      <template v-else>
        <div v-for="item in list" :key="item.id" class="notif-card"
             :class="{ unread: !item.isRead }" @click="handleRead(item)">
          <div class="notif-left">
            <div class="notif-type-icon" :class="item.type || 'system'">
              {{ typeIcon(item.type) }}
            </div>
          </div>
          <div class="notif-body">
            <div class="notif-header">
              <span class="notif-title">{{ item.title }}</span>
              <el-tag v-if="!item.isRead" size="small" type="danger" effect="plain">未读</el-tag>
            </div>
            <div class="notif-content">{{ item.content }}</div>
            <div class="notif-footer">
              <span class="notif-time">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </div>

        <div v-if="list.length === 0 && !loading" class="empty">
          <el-empty description="暂无通知"/>
        </div>

        <div class="pagination" v-if="total > size">
          <el-pagination
              v-model:current-page="current"
              :page-size="size"
              :total="total"
              layout="prev, pager, next"
              @current-change="onPageChange"
          />
        </div>
      </template>
    </div>

    <!-- 通知详情弹窗 -->
    <el-dialog v-model="detailVisible" title="通知详情" width="560px" :close-on-click-modal="true">
      <div class="detail-container" v-if="detailItem">
        <div class="detail-header">
          <el-tag :type="detailItem.type === 'system' ? '' : detailItem.type === 'order' ? 'warning' : 'success'"
                  size="small" effect="plain">
            {{ detailItem.type === 'system' ? '系统通知' : detailItem.type === 'order' ? '订单通知' : '促销通知' }}
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
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {ArrowLeft} from '@element-plus/icons-vue'
import {getNotificationList, markAsRead, markAllAsRead, getUnreadCount} from '@/api/notification.js'

const router = useRouter()
const activeTab = ref('all')
const list = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(10)
const loading = ref(false)
const unreadCount = ref(0)

// 详情弹窗
const detailVisible = ref(false)
const detailItem = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getNotificationList(current.value, size.value)
    if (res.success && res.data) {
      list.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    if (res.success) unreadCount.value = res.data || 0
  } catch (e) {
  }
}

const handleRead = async (item) => {
  // 先标记已读
  if (!item.isRead) {
    await markAsRead(item.id)
    item.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  // 打开详情弹窗
  detailItem.value = item
  detailVisible.value = true
}

const handleMarkAllRead = async () => {
  await markAllAsRead()
  list.value.forEach(item => item.isRead = true)
  unreadCount.value = 0
}

const onPageChange = (page) => {
  current.value = page
  loadData()
}

const goBack = () => router.back()

const typeIcon = (type) => {
  const map = {system: '📢', order: '📦', promotion: '🏷️'}
  return map[type] || '📢'
}

const formatTime = (t) => {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  loadData()
  loadUnreadCount()
})
</script>

<style scoped>
.notification-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.top-nav {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.nav-content {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
}

.breadcrumb {
  color: #999;
  font-size: 13px;
}

.notif-container {
  max-width: 800px;
  margin: 16px auto;
  padding: 0 16px;
}

.loading {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.notif-card {
  display: flex;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: box-shadow 0.2s;
  border-left: 3px solid transparent;
}

.notif-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.notif-card.unread {
  border-left-color: #5a6a7a;
  background: #f7f8fa;
}

.notif-left {
  margin-right: 14px;
}

.notif-type-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: #f0f2f5;
}

.notif-body {
  flex: 1;
  min-width: 0;
}

.notif-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.notif-title {
  font-weight: 600;
  font-size: 15px;
  color: #333;
}

.notif-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notif-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notif-time {
  font-size: 12px;
  color: #bbb;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.empty {
  padding: 60px 0;
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

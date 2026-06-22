<template>
  <div class="favorites-container">
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="goHome">
          <span>🏠</span>
          <h1>家具商城</h1>
        </div>
        <div class="nav-title">
          <span class="back-btn" @click="goBack">← 返回</span>
          <span class="divider">|</span>
          <span>我的收藏</span>
        </div>
        <div class="user-info">
          <span class="fav-count" v-if="list.length">共 {{ list.length }} 件</span>
        </div>
      </div>
    </header>

    <main class="main-content">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载收藏列表...</p>
      </div>
      <div v-else-if="list.length === 0" class="empty-state">
        <span class="empty-icon">💝</span>
        <p>还没有收藏任何家具</p>
        <el-button type="primary" @click="goHome">去逛逛</el-button>
      </div>
      <div v-else class="fav-grid">
        <div class="fav-card" v-for="item in list" :key="item.id" @click="goDetail(item)">
          <img :src="imgUrl(item.f_icon, '/images/default-furniture.png')"
               class="fav-img" @error="handleImgError"/>
          <div class="fav-info">
            <h3>{{ item.f_name }}</h3>
            <p class="fav-price">¥{{ formatPrice(item.price) }}</p>
          </div>
          <el-button type="danger" text size="small" @click.stop="handleRemove(item)" class="remove-btn">
            取消收藏
          </el-button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {getFavorites, toggleFavorite} from '@/api/favorite.js'
import {imgUrl} from '@/utils/img.js'

const router = useRouter()
const list = ref([])
const loading = ref(true)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getFavorites()
    if ((res.success || res.code === 200) && Array.isArray(res.data)) {
      list.value = res.data
    }
  } catch (e) {
    ElMessage.error('加载收藏失败')
  } finally {
    loading.value = false
  }
}

const handleRemove = async (item) => {
  try {
    await toggleFavorite(item.id)
    list.value = list.value.filter(i => i.id !== item.id)
    ElMessage.success('已取消收藏')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const formatPrice = (price) => {
  if (!price) return '0.00'
  return parseFloat(price).toFixed(2)
}

const goDetail = (item) => router.push({name: 'FurnitureDetail', params: {id: item.id}})
const goHome = () => router.push('/')
const goBack = () => router.back()

const handleImgError = (e) => {
  e.target.style.display = 'none'
}

onMounted(() => loadList())
</script>

<style scoped>
.favorites-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 14px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.logo h1 {
  font-size: 20px;
  color: #333;
  margin: 0;
}

.nav-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.back-btn {
  color: #5a6a7a;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  color: #333;
}

.divider {
  color: #ddd;
}

.fav-count {
  color: #999;
  font-size: 14px;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 80px 0;
}

.empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

.spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #eee;
  border-top-color: #5a6a7a;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 12px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.fav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.fav-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  border: 1px solid #f0f0f0;
  transition: all 0.2s;
}

.fav-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border-color: #d98a4a;
}

.fav-img {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  object-fit: cover;
  background: #f5f6f8;
  flex-shrink: 0;
}

.fav-info {
  flex: 1;
  min-width: 0;
}

.fav-info h3 {
  font-size: 15px;
  color: #333;
  margin: 0 0 6px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.fav-price {
  font-size: 16px;
  color: #d98a4a;
  font-weight: 600;
  margin: 0;
}

.remove-btn {
  flex-shrink: 0;
}
</style>

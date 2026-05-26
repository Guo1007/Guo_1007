<template>
    <div class="furniture-container">
        <!-- 顶部导航 -->
        <header class="header">
            <div class="header-content">
                <div class="logo" @click="goHome">
                    <h1>家具商城</h1>
                </div>
                <div class="nav-title">
                    <span class="back-btn" @click="goHome">← 返回首页</span>
                    <span class="divider">|</span>
                    <span class="current-type">{{ typeInfo.name }}</span>
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
        <main class="main-content">
            <div class="type-header text-only" v-if="typeInfo.name">
                <div class="type-info-center">
                    <h1>{{ typeInfo.name }}</h1>
                    <p class="type-desc">{{ typeInfo.title || '探索' + typeInfo.name + '系列的极致工艺与舒适体验' }}</p>
                </div>
            </div>

          <!--搜索面板 -->
            <div class="search-panel" :class="{ 'collapsed': !isSearchExpanded }">
                <div class="search-header" @click="toggleSearch">
                    <div class="search-title">
                        <span>筛选条件</span>
                        <span class="active-filters" v-if="activeFilterCount > 0">
                            {{ activeFilterCount }}个生效
                        </span>
                    </div>
                    <div class="search-toggle">
                        <span class="toggle-icon" :class="{ 'expanded': isSearchExpanded }">▼</span>
                    </div>
                </div>

                <div class="search-body" v-show="isSearchExpanded">
                    <div class="search-row">
                        <!-- 家具名称搜索 -->
                        <div class="search-item name-search">
                            <label>家具名称</label>
                            <div class="input-wrapper">
                                <input v-model="searchForm.fName" type="text" placeholder="输入名称关键词搜索..."
                                    @keyup.enter="handleSearch" />
                                <button class="clear-btn" v-if="searchForm.fName" @click.stop="searchForm.fName = ''">
                                    ✕
                                </button>
                            </div>
                        </div>

                        <!-- 库存状态 -->
                        <div class="search-item stock-search">
                            <label>库存状态</label>
                            <el-select v-model="searchForm.stockStatus" placeholder="全部" clearable>
                                <el-option label="全部" :value="null" />
                                <el-option label="有库存" value="in_stock" />
                                <el-option label="库存紧张(<10)" value="low_stock" />
                                <el-option label="无库存" value="out_stock" />
                            </el-select>
                        </div>

                        <!-- 品牌筛选 - 可搜索下拉 -->
                        <div class="search-item brand-search">
                            <label>品牌</label>
                            <el-select-v2 v-model="searchForm.brand" :options="brandOptions" placeholder="选择或输入品牌"
                                clearable filterable :filter-method="filterBrand" :height="240">
                                <template #default="{ item }">
                                    <div class="brand-option">
                                        <span class="brand-name">{{ item.label }}</span>
                                        <span class="brand-count" v-if="item.count">({{ item.count }}件)</span>
                                    </div>
                                </template>
                            </el-select-v2>
                        </div>
                    </div>

                    <div class="search-actions">
                        <el-button type="primary" @click="handleSearch" :loading="loading" size="large">
                          <span></span> 搜索
                        </el-button>
                        <el-button @click="resetSearch" size="large">重置</el-button>
                    </div>
                </div>
            </div>

            <!-- 家具列表 -->
            <div class="furniture-section">
                <div class="section-title">
                    <h2>精选家具</h2>
                    <span class="count" v-if="!loading">共 {{ total }} 件</span>
                </div>

                <!-- 加载状态 -->
                <div v-if="loading" class="loading-state">
                    <div class="spinner"></div>
                    <p>正在加载家具列表...</p>
                </div>

                <!-- 空状态 -->
                <div v-else-if="furnitureList.length === 0 && !loading" class="empty-state">
                    <div class="empty-icon">📦</div>
                    <p>该分类暂无家具</p>
                    <p class="empty-tip" v-if="hasActiveFilters">当前筛选条件无匹配结果</p>
                    <button class="clear-filters-btn" v-if="hasActiveFilters" @click="resetSearchAndLoad">
                        清除筛选条件
                    </button>
                    <button class="back-home-btn" v-else @click="goHome">去其他分类看看</button>
                </div>

                <!-- 家具网格 -->
                <div v-else class="furniture-grid">
                    <div class="furniture-card" v-for="item in furnitureList" :key="item.id" @click="goToDetail(item)">
                        <div class="furniture-image">
                          <img :src="imgUrl(item.fIcon)" :alt="item.fName"
                                class="furniture-img-real" @error="handleImgError" />
                            <div v-if="!item.fIcon" class="image-placeholder">
                                <span>🪑</span>
                            </div>
                            <div class="image-overlay"></div>
                            <div class="stock-badge"
                                :class="{ 'low-stock': item.stock < 10 && item.stock > 0, 'out-stock': item.stock === 0 }">
                                {{ item.stock === 0 ? '缺货' : item.stock < 10 ? '库存紧张' : '库存 ' + item.stock }} </div>
                            </div>
                            <div class="furniture-info">
                                <h3 class="furniture-name">{{ item.fName }}</h3>
                                <p class="furniture-brand" v-if="item.brand">
                                    <span>🏷️</span> {{ item.brand }}
                                </p>
                                <p class="furniture-intro" v-if="item.intro">{{ item.intro }}</p>
                                <div class="furniture-footer">
                                    <span class="price">¥{{ formatPrice(item.price) }}</span>
                                    <button class="view-btn">查看详情</button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="pagination-wrapper" v-if="total > 0">
                        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
                            :page-sizes="[5, 10, 20, 50]" layout="total, sizes, prev, pager, next, jumper"
                            :total="total" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
                    </div>
                </div>
        </main>
    </div>
    <CartDrawer />
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {getFurnitureBrands, getFurnitureByTypeId} from '@/api/furniture.js'
import {imgUrl} from '@/utils/img.js'


import CartDrawer from '@/components/CartDrawer.vue'
import {useCartStore} from '@/stores/cart.js'

const cartStore = useCartStore()
const route = useRoute()
const router = useRouter()
const typeId = ref(route.params.id)
const isAllCategories = computed(() => typeId.value === '0')

const userName = ref('用户')
const userIcon = ref('/images/default-avatar.png')

const isSearchExpanded = ref(false)
const allBrands = ref([])
const brandOptions = ref([])

const searchForm = ref({
  fName: route.query.keyword || '',
    stockStatus: null,
    brand: undefined
})

const furnitureList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const typeInfo = ref({})

const activeFilterCount = computed(() => {
    let count = 0
    if (searchForm.value.fName?.trim()) count++
    if (searchForm.value.stockStatus) count++
    if (searchForm.value.brand) count++
    return count
})

const hasActiveFilters = computed(() => activeFilterCount.value > 0)

const toggleSearch = () => {
    isSearchExpanded.value = !isSearchExpanded.value
}

const loadBrands = async () => {
    try {
        const res = await getFurnitureBrands(typeId.value)
        if (res.success || res.code === 200) {
            const brandNames = res.data || []
            brandOptions.value = brandNames.map(name => ({
                value: name,
                label: name
            })).sort((a, b) => a.label.localeCompare(b.label, 'zh-CN'))
            allBrands.value = brandNames.map(name => ({ name }))
        }
    } catch (error) {
        console.error('加载品牌失败:', error)
    }
}

const filterBrand = (query) => {
    if (!query) {
        loadBrands()
        return
    }

    const lowerQuery = query.toLowerCase()
    brandOptions.value = allBrands.value
        .filter(brand => brand.name.toLowerCase().includes(lowerQuery))
        .map(brand => ({
            value: brand.name,
            label: brand.name
        }))
}

const loadFurnitureList = async () => {
    if (!typeId.value) {
        ElMessage.error('分类ID不能为空')
        loading.value = false
        return
    }
    try {
        loading.value = true
        const params = {
            current: currentPage.value,
            size: pageSize.value
        }
      if (isAllCategories.value) {
        params.typeId = 0
        if (searchForm.value.fName?.trim()) {
          params.keyword = searchForm.value.fName.trim()
        }
      } else {
        params.typeId = typeId.value
        if (searchForm.value.fName?.trim()) {
          params.fName = searchForm.value.fName.trim()
        }
        }
        if (searchForm.value.stockStatus) {
            params.stockStatus = searchForm.value.stockStatus
        }
        if (searchForm.value.brand) {
            params.brand = searchForm.value.brand
        }

        const res = await getFurnitureByTypeId(params)

        if ((res.success || res.code === 200) && res.data) {
            furnitureList.value = res.data.records || []
            total.value = res.data.total || 0

            if (furnitureList.value.length === 0 && currentPage.value > 1) {
                currentPage.value = 1
                loadFurnitureList()

            }
        } else {
            furnitureList.value = []
            total.value = 0
        }
    } catch (error) {
        console.error('加载家具列表失败:', error)
        ElMessage.error('加载失败，请稍后重试')
        furnitureList.value = []
        total.value = 0
    } finally {
        loading.value = false
    }
}

const handleSearch = () => {
    currentPage.value = 1
    loadFurnitureList()
}

const resetSearch = () => {
    searchForm.value = {
        fName: '',
        stockStatus: null,
        brand: undefined
    }
    handleSearch()
}

const resetSearchAndLoad = () => {
    resetSearch()
}

const handleSizeChange = (val) => {
    pageSize.value = val
    currentPage.value = 1
    loadFurnitureList()
}

const handleCurrentChange = (val) => {
    currentPage.value = val
    loadFurnitureList()
}

const loadTypeInfo = () => {
  if (isAllCategories.value) {
    typeInfo.value = {name: '全部商品', title: '搜索浏览所有家具'}
    return
  }
    const cached = sessionStorage.getItem('currentType')
    if (cached) {
        const parsed = JSON.parse(cached)
        if (parsed.id == typeId.value) {
            typeInfo.value = parsed
        }
    }
    if (!typeInfo.value.name) {
        typeInfo.value = { name: '家具系列' }
    }
}

const formatPrice = (price) => {
    if (!price) return '0.00'
    return parseFloat(price).toFixed(2)
}

const goToDetail = (item) => {
    router.push({
        name: 'FurnitureDetail',
        params: { id: item.id }
    })
}

const goHome = () => router.push('/')

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
    const placeholder = e.target.parentElement.querySelector('.image-placeholder')
    if (placeholder) placeholder.style.display = 'flex'
}

const goToProfile = () => {
    router.push('/user/profile')
}

watch(() => route.params.id, (newId) => {
    if (newId) {
        typeId.value = newId
      const keyword = route.query.keyword || ''
      searchForm.value = {fName: keyword, stockStatus: null, brand: undefined}
      isSearchExpanded.value = !!keyword
        currentPage.value = 1
        loadTypeInfo()
      if (!isAllCategories.value) {
        loadBrands()
      }
        loadFurnitureList()
    }
})

onMounted(() => {
    loadUserInfo()
    loadTypeInfo()
  if (!isAllCategories.value) {
    loadBrands()
  }
  if (route.query.keyword) {
    isSearchExpanded.value = true
  }
    loadFurnitureList()
})
</script>

<style scoped>
.furniture-container {
  min-height: 100vh;
  background: #f5f5f5;
}

/* ===== 顶部导航 ===== */
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.logo span {
  font-size: 24px;
}

.logo h1 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.nav-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #666;
}

.back-btn {
  cursor: pointer;
  color: #5a6a7a;
  transition: color 0.2s;
}

.back-btn:hover {
  color: #333;
}

.divider {
  color: #ddd;
}

.current-type {
  font-weight: 500;
  color: #333;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-profile:hover {
  background: #f5f5f5;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.welcome {
  font-size: 14px;
  color: #333;
}

/* ===== 主体内容 ===== */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
}

/* ===== 分类头部 ===== */
.type-header {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.type-info-center h1 {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.type-desc {
  font-size: 16px;
  color: #666;
}

/* ===== 搜索面板 ===== */
.search-panel {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.search-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  cursor: pointer;
  transition: background 0.2s;
}

.search-header:hover {
  background: #fafafa;
}

.search-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.search-icon {
  font-size: 16px;
}

.active-filters {
  font-size: 12px;
  color: #5a6a7a;
  background: #e8f0fe;
  padding: 2px 8px;
  border-radius: 10px;
}

.toggle-icon {
  font-size: 12px;
  color: #999;
  transition: transform 0.3s;
}

.toggle-icon.expanded {
  transform: rotate(180deg);
}

.search-body {
  padding: 0 24px 24px;
  border-top: 1px solid #f0f0f0;
}

.search-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.search-item label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.input-wrapper {
  position: relative;
}

.clear-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 12px;
}

.search-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* ===== 家具列表 ===== */
.furniture-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.section-title span {
  font-size: 20px;
}

.section-title h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.count {
  font-size: 14px;
  color: #999;
  margin-left: auto;
}

/* ===== 加载状态 ===== */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 60px 0;
  color: #999;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f0f0f0;
  border-top-color: #5a6a7a;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ===== 空状态 ===== */
.empty-state {
  text-align: center;
  padding: 60px 0;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: #666;
  margin-bottom: 8px;
}

.empty-tip {
  font-size: 14px;
  color: #999;
}

.clear-filters-btn,
.back-home-btn {
  margin-top: 16px;
  padding: 10px 24px;
  background: #5a6a7a;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.clear-filters-btn:hover,
.back-home-btn:hover {
  background: #4a5a6a;
}

/* ===== 家具网格 ===== */
.furniture-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.furniture-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.furniture-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
}

.furniture-image {
  position: relative;
  height: 200px;
  background: #f5f5f5;
  overflow: hidden;
}

.furniture-img-real {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  background: #f5f5f5;
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.1));
}

.stock-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  color: #333;
}

.stock-badge.low-stock {
  background: #fff3e0;
  color: #e65100;
}

.stock-badge.out-stock {
  background: #ffebee;
  color: #c62828;
}

.furniture-info {
  padding: 16px;
}

.furniture-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.furniture-brand {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.furniture-brand span {
  margin-right: 4px;
}

.furniture-intro {
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.furniture-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price {
  font-size: 20px;
  font-weight: 700;
  color: #e74c3c;
}

.view-btn {
  padding: 6px 16px;
  background: #5a6a7a;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}

.view-btn:hover {
  background: #4a5a6a;
}

/* ===== 分页 ===== */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0 8px;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .header-content {
    flex-wrap: wrap;
    height: auto;
    padding: 12px 16px;
    gap: 8px;
  }

  .nav-title {
    order: 3;
    width: 100%;
    justify-content: center;
  }

  .main-content {
    padding: 16px;
  }

  .type-header {
    padding: 24px 16px;
  }

  .type-info-center h1 {
    font-size: 24px;
  }

  .search-row {
    grid-template-columns: 1fr;
  }

  .furniture-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .furniture-image {
    height: 150px;
  }
}
</style>
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

            <!-- 🔍 搜索面板 -->
            <div class="search-panel" :class="{ 'collapsed': !isSearchExpanded }">
                <div class="search-header" @click="toggleSearch">
                    <div class="search-title">
                        <span class="search-icon">🔍</span>
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
                            <span>🔍</span> 搜索
                        </el-button>
                        <el-button @click="resetSearch" size="large">重置</el-button>
                    </div>
                </div>
            </div>

            <!-- 家具列表 -->
            <div class="furniture-section">
                <div class="section-title">
                    <span>🛋️</span>
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
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFurnitureByTypeId, getFurnitureBrands } from '@/api/furniture.js'
import {imgUrl} from '@/utils/img.js'


import CartDrawer from '@/components/CartDrawer.vue'
import { useCartStore } from '@/stores/cart.js'

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
                return
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
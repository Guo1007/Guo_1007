<template>
    <div class="home-container">
        <!-- 顶部导航 -->
        <header class="header">
            <div class="header-content">
              <div class="logo" @click="goHome">
                    <h1>家具商城</h1>
                </div>
                <div class="user-info">
                  <div class="user-profile" @click="goToProfile">
                    <el-avatar :size="36" :src="userIcon">
                      <img src="/images/default-avatar.png"/>
                    </el-avatar>
                    <span class="welcome">{{ userName }}</span>
                    <span class="welcome-label">欢迎回来</span>
                  </div>
                  <div class="header-divider"></div>
                  <NotificationBell/>
                  <div class="header-divider"></div>
                  <el-button v-if="isAdmin" plain class="admin-btn" @click="goToAdmin">
                    后台管理
                  </el-button>
                  <el-button text class="logout-btn" @click="handleLogout">
                    退出
                  </el-button>
                </div>
            </div>
        </header>

      <!-- 横幅 Hero -->
      <section class="hero-banner">
        <div class="hero-overlay"></div>
        <div class="hero-content">
          <h2>品质家具，从这里开始</h2>
          <p>精选优质家居好物，打造理想生活空间</p>
          <div class="hero-search">
            <input v-model="searchKeyword" type="text" placeholder="搜索你想要的家具..."
                   @keyup.enter="handleSearch"/>
            <button class="search-btn" @click="handleSearch">搜索</button>
          </div>
        </div>
      </section>

      <!-- 主体内容 -->
        <main class="main-content">
          <div class="section-header">
            <h2>家具分类</h2>
            <span class="section-subtitle">选择你感兴趣的品类</span>
          </div>

          <div v-if="loading" class="loading-tip">
            <el-icon class="loading-icon" :size="24">
              <Loading/>
            </el-icon>
            <span>正在加载家具分类...</span>
          </div>

          <div class="feature-grid" v-else-if="features.length > 0">
                <div class="feature-card" v-for="(item, index) in features" :key="item.id || index"
                    @click="goToTypeDetail(item)">
                  <div class="feature-icon-bg">
                    <div class="feature-icon">
                      <span v-if="item.icon && !item.icon.startsWith('/') && !item.icon.startsWith('http')">{{
                          item.icon
                        }}</span>
                      <img v-else-if="item.icon" :src="imgUrl(item.icon)" alt="图标" class="icon-img"/>
                    </div>
                  </div>
                    <h3>{{ item.name }}</h3>
                    <p class="feature-desc">{{ item.title || item.name + '系列家具' }}</p>
                </div>
          </div>
          <div v-else class="empty-tip">
            <el-empty description="暂无家具分类数据"/>
            </div>
        </main>

      <!-- 底部 -->
      <footer class="footer">
        <span>家具商城 &copy; 2026 — 用心打造每一件家具</span>
      </footer>
    </div>
    <CartDrawer />
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Loading} from '@element-plus/icons-vue'
import {imgUrl} from '@/utils/img.js'
import {getUserInfo, userLogout} from '@/api/user.js'
import {getFurnitureTypeList} from '@/api/furniture.js'

import CartDrawer from '@/components/CartDrawer.vue'
import NotificationBell from '@/components/NotificationBell.vue'
import {useCartStore} from '@/stores/cart.js'

const cartStore = useCartStore()
const router = useRouter()

const userName = ref('用户')
const userIcon = ref('/images/default-avatar.png')
const features = ref([])
const loading = ref(true)
const searchKeyword = ref('')

onMounted(() => {
    loadUserInfo()
    loadFurnitureTypes()
})

const isAdmin = computed(() => {
    const userInfoStr = localStorage.getItem('userInfo')
    if (!userInfoStr) return false
    try {
        const userInfo = JSON.parse(userInfoStr)
        return userInfo.isAdmin === 1
    } catch (e) {
        return false
    }
})

const goToAdmin = () => {
    router.push('/admin')
}

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
    } else {
        userName.value = localStorage.getItem('userName') || '用户'
        userIcon.value = localStorage.getItem('userIcon') || '/images/default-avatar.png'
    }
    refreshUserInfo()
}

const refreshUserInfo = async () => {
    try {
        const res = await getUserInfo()
        if (res.success && res.data) {
            const userInfo = res.data
            userName.value = userInfo.userName || '用户'
          userIcon.value = imgUrl(userInfo.icon, '/images/default-avatar.png')
            localStorage.setItem('userInfo', JSON.stringify(userInfo))
            localStorage.setItem('userName', userName.value)
            localStorage.setItem('userIcon', userIcon.value)
        }
    } catch (error) {
      // 静默失败，使用本地缓存数据
    }
}

const handleLogout = () => {
    ElMessageBox.confirm(
        '确定要退出登录吗？',
        '提示',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        }
    )
        .then(async () => {
            try {
                await userLogout()
                localStorage.removeItem('token')
                localStorage.removeItem('userInfo')
                localStorage.removeItem('userName')
                localStorage.removeItem('userIcon')
              localStorage.removeItem('userEmail')
                sessionStorage.clear()
                cartStore.clearState()
                ElMessage.success('已安全退出')
                setTimeout(() => {
                    router.push('/login')
                }, 500)
            } catch (error) {
                localStorage.removeItem('token')
                localStorage.removeItem('userInfo')
                localStorage.removeItem('userName')
                localStorage.removeItem('userIcon')
              localStorage.removeItem('userEmail')
                ElMessage.warning('本地已退出，但服务器同步失败，请重新登录以确保安全')
                setTimeout(() => {
                    router.push('/login')
                }, 500)
            }
        })
        .catch(() => {
        })
}

const loadFurnitureTypes = async () => {
    try {
        loading.value = true
        const res = await getFurnitureTypeList()
        if (res.success && Array.isArray(res.data)) {
            features.value = res.data
        } else if (res.code === 200 && Array.isArray(res.data)) {
            features.value = res.data
        } else {
            features.value = []
        }
    } catch (error) {
        console.error('加载家具分类失败:', error)
        ElMessage.error('加载分类失败，请稍后重试')
        features.value = []
    } finally {
        loading.value = false
    }
}

const goToTypeDetail = (item) => {
    sessionStorage.setItem('currentType', JSON.stringify({
        id: item.id,
        name: item.name,
        icon: item.icon,
        title: item.title
    }))
  router.push({path: `/type/${item.id}`})
}

const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (keyword) {
    router.push({path: '/type/0', query: {keyword}})
  } else {
    router.push('/type/0')
  }
}

const goToProfile = () => {
    router.push('/user/profile')
}
</script>

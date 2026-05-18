<template>
    <div class="home-container">
        <!-- 顶部导航 -->
        <header class="header">
            <div class="header-content">
                <div class="logo">
                    <span>🏠</span>
                    <h1>家具商城</h1>
                </div>
                <div class="user-info">
                    <img :src="userIcon" class="user-avatar" alt="头像" @error="handleImageError" @click="goToProfile"
                        style="cursor: pointer;" />
                    <span class="welcome">欢迎回来，{{ userName }}</span>
                    <template v-if="isAdmin">
                        <el-button type="primary" size="small" @click="goToAdmin" style="margin: 0 10px;">
                            🛠️ 后台管理
                        </el-button>
                    </template>
                    <button class="logout-btn" @click="handleLogout">
                        <span>🚪</span>
                        退出登录
                    </button>
                </div>
            </div>
        </header>

        <!-- 主体内容 -->
        <main class="main-content">
            <div v-if="loading" class="loading-tip">正在加载家具分类...</div>

            <div class="feature-grid" v-else>
                <div class="feature-card" v-for="(item, index) in features" :key="item.id || index"
                    @click="goToTypeDetail(item)">
                    <div class="feature-icon">
                        <span v-if="item.icon && !item.icon.startsWith('/')">{{ item.icon }}</span>
                        <img v-else :src="'http://localhost:8080' + item.icon" alt="图标" class="icon-img" />
                    </div>

                    <h3>{{ item.name }}</h3>
                    <p class="feature-desc">{{ item.title || item.name + '系列家具' }}</p>
                </div>
                <div v-if="!loading && features.length === 0" class="empty-tip">
                    暂无家具分类数据
                </div>
            </div>
        </main>
    </div>
    <CartDrawer />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserInfo, userLogout } from '@/api/user.js'
import { getFurnitureTypeList } from '@/api/furniture.js'
import '@/styles/views/home.scss'
import CartDrawer from '@/components/CartDrawer.vue'
import { useCartStore } from '@/stores/cart.js'
import { computed } from 'vue'

const cartStore = useCartStore()


const router = useRouter()

const userName = ref('用户')
const userIcon = ref('/images/default-avatar.png')

const features = ref([])
const loading = ref(true)

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
            userIcon.value = userInfo.icon ? 'http://localhost:8080' + userInfo.icon : '/images/default-avatar.png'
            console.log('从缓存读取用户信息:', userInfo)
        } catch (e) {
            console.error('解析用户信息失败:', e)
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
            userIcon.value = userInfo.icon ? 'http://localhost:8080' + userInfo.icon : '/images/default-avatar.png'
            localStorage.setItem('userInfo', JSON.stringify(userInfo))
            localStorage.setItem('userName', userName.value)
            localStorage.setItem('userIcon', userIcon.value)

            console.log('刷新用户信息成功:', userInfo)
        }
    } catch (error) {
        console.error('刷新用户信息失败:', error)
    }
}

const handleImageError = (e) => {
    e.target.src = '/images/default-avatar.png'
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
                sessionStorage.clear()
                cartStore.clearState()
                ElMessage.success('已安全退出')
                setTimeout(() => {
                    router.push('/login')
                }, 500)
            } catch (error) {
                console.error('退出登录失败:', error)
                localStorage.removeItem('token')
                localStorage.removeItem('userInfo')
                localStorage.removeItem('userName')
                localStorage.removeItem('userIcon')
                ElMessage.warning('本地已退出，但服务器同步失败，请重新登录以确保安全')
                setTimeout(() => {
                    router.push('/login')
                }, 500)
            }
        })
        .catch(() => {
            ElMessage.info('已取消退出')
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
            if (!res.success) {
                ElMessage.warning(res.message || '暂无家具分类数据')
            }
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
    router.push({
        path: `/type/${item.id}`
    })
}

const goToProfile = () => {
    router.push('/user/profile')
}
</script>

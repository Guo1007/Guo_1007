<template>
  <div class="admin-layout">
    <!-- 顶部 -->
    <header class="admin-header">
      <div class="header-left">
        <span class="mobile-menu-btn" @click="sidebarOpen = !sidebarOpen">☰</span>
        <span class="logo">🏠</span>
        <h1>家具商城后台管理</h1>
      </div>
      <div class="header-right">
        <span class="admin-info">{{ adminName }}</span>
        <el-tooltip content="返回系统主页" placement="bottom" effect="dark">
          <span class="home-icon-btn" @click="goHome">🏠</span>
        </el-tooltip>
        <el-button type="danger" size="small" @click="logout">退出</el-button>
      </div>
    </header>

    <div class="admin-body">
      <!-- 移动端遮罩 -->
      <div class="sidebar-overlay" v-if="sidebarOpen" @click="sidebarOpen = false"></div>
      <!-- 侧边栏 -->
      <aside class="admin-sidebar" :class="{ 'mobile-open': sidebarOpen }">
        <router-link v-for="menu in menus" :key="menu.path" :to="menu.path" class="menu-item"
                     :class="{ active: $route.path === menu.path }" @click="sidebarOpen = false">
          <span class="menu-icon">{{ menu.icon }}</span>
          <span class="menu-text">{{ menu.name }}</span>
        </router-link>
      </aside>

      <!-- 内容区 -->
      <main class="admin-main">
        <router-view/>
      </main>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {useLogout} from '@/composables/useLogout.js'


const router = useRouter()

const adminName = ref('管理员')
const sidebarOpen = ref(false)

const menus = [
  {path: '/admin/dashboard', name: '数据概览'},
  {path: '/admin/users', name: '用户管理'},
  {path: '/admin/furniture', name: '家具管理'},
  {path: '/admin/orders', name: '订单管理'},
  {path: '/admin/furniture_type', name: '家具类型管理'},
  {path: '/admin/notification', name: '通知管理'},
  {path: '/admin/comments', name: '评价审核'},
]

const goHome = () => {
  router.push('/')
}

const { logout } = useLogout()
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: #f5f7fa;
}

.admin-header {
  height: 60px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left .logo {
  font-size: 28px;
}

.header-left h1 {
  font-size: 18px;
  color: #333;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-info {
  color: #666;
  font-size: 14px;
}

.admin-body {
  display: flex;
  padding-top: 60px;
  min-height: 100vh;
}

.admin-sidebar {
  width: 200px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  padding: 16px 0;
  position: fixed;
  left: 0;
  top: 60px;
  bottom: 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 24px;
  color: #666;
  text-decoration: none;
  transition: all 0.2s;
}

.menu-item:hover {
  background: #f5f7fa;
  color: #5a6a7a;
}

.menu-item.active {
  background: #eef0f2;
  color: #5a6a7a;
  border-right: 3px solid #5a6a7a;
}

.menu-icon {
  font-size: 18px;
}

.menu-text {
  font-size: 14px;
}

.admin-main {
  flex: 1;
  margin-left: 200px;
  padding: 24px;
}

.home-icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f2f5;
  color: #909399;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s;
  margin-left: 10px;
}

.home-icon-btn:hover {
  background: #5a6a7a;
  color: #fff;
}

/* 移动端菜单按钮 - 默认隐藏 */
.mobile-menu-btn {
  display: none;
  font-size: 22px;
  cursor: pointer;
  color: #666;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.2s;
}

.mobile-menu-btn:hover {
  background: #f0f2f5;
  color: #333;
}

/* 遮罩层 - 默认隐藏 */
.sidebar-overlay {
  display: none;
}

@media (max-width: 768px) {
  .mobile-menu-btn {
    display: inline-block;
  }

  .header-left h1 {
    font-size: 15px;
  }

  .admin-info {
    display: none;
  }

  .sidebar-overlay {
    display: block;
    position: fixed;
    top: 60px;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.3);
    z-index: 99;
  }

  .admin-sidebar {
    z-index: 100;
    transition: transform 0.25s ease;
  }
}
</style>
<template>
  <div class="admin-layout">
    <!-- 顶部 -->
    <header class="admin-header">
      <div class="header-left">
        <span class="mobile-menu-btn" @click="sidebarOpen = !sidebarOpen"
          >☰</span
        >
        <router-link to="/" class="header-brand">
          <img
            v-if="sys.systemLogo"
            :src="imgUrl(sys.systemLogo)"
            class="brand-logo"
            alt=""
          />
          <span v-else class="brand-mark">木</span>
          <span class="brand-name">{{ sys.systemName }}</span>
          <span class="brand-dot">后台</span>
        </router-link>
      </div>
      <div class="header-right">
        <router-link to="/" class="home-link" title="返回前台">
          <svg
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <polyline points="9 22 9 12 15 12 15 22" />
          </svg>
        </router-link>
        <span class="admin-name">{{ adminName }}</span>
        <button class="logout-btn" @click="logout">退出</button>
      </div>
    </header>

    <div class="admin-body">
      <!-- 遮罩 -->
      <div
        class="sidebar-overlay"
        v-if="sidebarOpen"
        @click="sidebarOpen = false"
      ></div>

      <!-- 侧边栏 -->
      <aside class="admin-sidebar" :class="{ 'mobile-open': sidebarOpen }">
        <nav class="sidebar-nav">
          <router-link
            v-for="menu in menus"
            :key="menu.path"
            :to="menu.path"
            class="menu-item"
            :class="{ active: $route.path === menu.path }"
            @click="sidebarOpen = false"
          >
            <span class="menu-icon" v-html="menu.icon"></span>
            <span class="menu-text">{{ menu.name }}</span>
            <span v-if="menu.badge > 0" class="menu-badge">{{
              menu.badge
            }}</span>
          </router-link>
        </nav>
      </aside>

      <!-- 内容区 -->
      <main class="admin-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { useSystemStore } from "@/stores/system.js";
import { imgUrl } from "@/utils/img.js";
import { useLogout } from "@/composables/useLogout.js";
import { getPendingOrderCount } from "@/api/admin/order.js";
import { getPendingCommentCount } from "@/api/admin/comment.js";

const router = useRouter();
const sys = useSystemStore();

const adminName = ref("管理员");
const sidebarOpen = ref(false);

// SVG icons for each menu item
const menus = ref([
  {
    path: "/admin/dashboard",
    name: "数据概览",
    icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="9" rx="1"/><rect x="14" y="8" width="7" height="4" rx="1"/><rect x="14" y="3" width="7" height="4" rx="1"/><rect x="3" y="13" width="7" height="8" rx="1"/></svg>',
    badge: 0,
  },
  {
    path: "/admin/users",
    name: "用户管理",
    icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>',
    badge: 0,
  },
  {
    path: "/admin/furniture",
    name: "家具管理",
    icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 3h7v7H3z"/><path d="M14 3h7v7h-7z"/><path d="M14 14h7v7h-7z"/><path d="M3 14h7v7H3z"/></svg>',
    badge: 0,
  },
  {
    path: "/admin/furniture_type",
    name: "分类管理",
    icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="4 7 12 4 20 7"/><polyline points="4 7 12 20 20 7"/><line x1="4" y1="7" x2="4" y2="17"/><line x1="20" y1="7" x2="20" y2="17"/><line x1="12" y1="20" x2="12" y2="17"/></svg>',
    badge: 0,
  },
  {
    path: "/admin/orders",
    name: "订单管理",
    icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="1" y="3" width="15" height="13" rx="2"/><polygon points="23 7 16 12 23 17 23 7"/></svg>',
    badge: 0,
  },
  {
    path: "/admin/comments",
    name: "评价审核",
    icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>',
    badge: 0,
  },
  {
    path: "/admin/notification",
    name: "通知管理",
    icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>',
    badge: 0,
  },
  {
    path: "/admin/site-content",
    name: "首页内容",
    icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="4 17 10 11 14 15 20 9"/><polyline points="14 9 20 9 20 15"/><line x1="4" y1="21" x2="20" y2="21"/></svg>',
    badge: 0,
  },
]);

let countTimer = null;

const fetchPendingCounts = async () => {
  try {
    const [orderRes, commentRes] = await Promise.all([
      getPendingOrderCount(),
      getPendingCommentCount(),
    ]);
    const orderMenu = menus.value.find((m) => m.path === "/admin/orders");
    const commentMenu = menus.value.find((m) => m.path === "/admin/comments");
    if (orderRes.success || orderRes.code === 200) {
      orderMenu.badge = orderRes.data?.pendingShipCount || 0;
    }
    if (commentRes.success || commentRes.code === 200) {
      const d = commentRes.data || {};
      commentMenu.badge =
        (d.commentCount || 0) +
        (d.appendCount || 0) +
        (d.reviewCommentCount || 0);
    }
  } catch (e) {
    /* ignore */
  }
};

onMounted(() => {
  sys.load();
  fetchPendingCounts();
  countTimer = setInterval(fetchPendingCounts, 60000);
});

onBeforeUnmount(() => {
  if (countTimer) clearInterval(countTimer);
});

const { logout } = useLogout();
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: var(--color-bg);
}

/* ===== Header ===== */
.admin-header {
  height: 56px;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-6);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}
.header-brand {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  text-decoration: none;
}
.brand-mark {
  width: 28px;
  height: 28px;
  background: var(--color-dark);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  border-radius: var(--radius-sm);
  font-family: var(--font-serif);
}
.brand-logo {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  object-fit: contain;
}
.brand-name {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}
.brand-dot {
  font-size: 11px;
  color: var(--color-text-tertiary);
  background: var(--color-border-light);
  padding: 0 6px;
  border-radius: 10px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}
.home-link {
  display: flex;
  align-items: center;
  color: var(--color-text-tertiary);
  transition: color var(--transition-fast);
}
.home-link:hover {
  color: var(--color-text-primary);
}
.admin-name {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
}
.logout-btn {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}
.logout-btn:hover {
  color: var(--color-danger);
  background: #fef5f5;
}

/* ===== Body ===== */
.admin-body {
  display: flex;
  padding-top: 56px;
  min-height: 100vh;
}

/* ===== Sidebar ===== */
.admin-sidebar {
  width: 200px;
  background: var(--color-surface);
  border-right: 1px solid var(--color-border-light);
  position: fixed;
  left: 0;
  top: 56px;
  bottom: 0;
  overflow-y: auto;
}

.sidebar-nav {
  padding: var(--space-3) 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin: 0 var(--space-2);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  text-decoration: none;
  font-size: var(--text-sm);
  transition: all var(--transition-fast);
}
.menu-item:hover {
  background: var(--color-bg);
  color: var(--color-text-primary);
}
.menu-item.active {
  background: var(--color-dark);
  color: #fff;
}
.menu-item.active .menu-badge {
  background: rgba(255, 255, 255, 0.25);
}

.menu-icon {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
.menu-text {
  flex: 1;
}
.menu-badge {
  min-width: 18px;
  height: 18px;
  background: var(--color-accent);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
}

/* ===== Main ===== */
.admin-main {
  flex: 1;
  margin-left: 200px;
  padding: var(--space-6);
  min-width: 0;
}

/* ===== Mobile ===== */
.mobile-menu-btn {
  display: none;
  font-size: 20px;
  cursor: pointer;
  color: var(--color-text-secondary);
  padding: 4px 6px;
  border-radius: var(--radius-sm);
}
.mobile-menu-btn:hover {
  background: var(--color-bg);
}
.sidebar-overlay {
  display: none;
}

@media (max-width: 768px) {
  .mobile-menu-btn {
    display: inline-block;
  }
  .brand-name,
  .brand-dot,
  .admin-name {
    display: none;
  }
  .sidebar-overlay {
    display: block;
    position: fixed;
    top: 56px;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.2);
    z-index: 99;
  }
  .admin-sidebar {
    z-index: 100;
    transform: translateX(-100%);
    transition: transform 0.25s ease;
  }
  .admin-sidebar.mobile-open {
    transform: translateX(0);
  }
  .admin-main {
    margin-left: 0;
    padding: var(--space-4);
  }
}
</style>

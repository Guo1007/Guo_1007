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
        <span class="admin-name">{{ adminName }}</span>
        <router-link to="/" class="header-action-btn" title="返回前台">
          <svg
            width="15"
            height="15"
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
          <span>返回前台</span>
        </router-link>
        <button class="header-action-btn logout-btn" @click="logout">
          <svg
            width="15"
            height="15"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line x1="21" y1="12" x2="9" y2="12" />
          </svg>
          <span>退出</span>
        </button>
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
          <template v-for="group in menuGroups" :key="group.title">
            <!-- 无标题分组：直接平铺 -->
            <template v-if="!group.title">
              <router-link
                v-for="menu in group.items"
                :key="menu.path"
                :to="menu.path"
                class="menu-item"
                :class="{ active: $route.path === menu.path }"
                @click="sidebarOpen = false"
              >
                <span class="menu-icon" v-html="menu.icon"></span>
                <span class="menu-text">{{ menu.name }}</span>
                <span v-if="menu.badge > 0" class="menu-badge">{{ menu.badge }}</span>
              </router-link>
            </template>

            <!-- 有标题分组：可折叠 -->
            <template v-else>
              <div
                class="menu-group-title"
                :class="{ open: expandedGroups[group.title] }"
                @click="toggleGroup(group.title)"
              >
                <span class="group-caret">▸</span>
                <span class="group-name">{{ group.title }}</span>
              </div>
              <div class="menu-group-items" :class="{ open: expandedGroups[group.title] }">
                <div class="menu-group-inner">
                  <router-link
                    v-for="menu in group.items"
                    :key="menu.path"
                    :to="menu.path"
                    class="menu-item"
                    :class="{ active: $route.path === menu.path }"
                    @click="sidebarOpen = false"
                  >
                    <span class="menu-icon" v-html="menu.icon"></span>
                    <span class="menu-text">{{ menu.name }}</span>
                    <span v-if="menu.badge > 0" class="menu-badge">{{ menu.badge }}</span>
                  </router-link>
                </div>
              </div>
            </template>
          </template>
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
import { getPendingOrderCount, getPendingRefundCount } from "@/api/admin/order.js";
import { getPendingCommentCount } from "@/api/admin/comment.js";

const router = useRouter();
const sys = useSystemStore();

const adminName = ref("管理员");
const sidebarOpen = ref(false);

// SVG icons for each menu item, grouped by business domain
const menuGroups = ref([
  {
    title: "",
    items: [
      {
        path: "/admin/dashboard",
        name: "数据概览",
        icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="9" rx="1"/><rect x="14" y="8" width="7" height="4" rx="1"/><rect x="14" y="3" width="7" height="4" rx="1"/><rect x="3" y="13" width="7" height="8" rx="1"/></svg>',
        badge: 0,
      },
    ],
  },
  {
    title: "商品管理",
    items: [
      {
        path: "/admin/furniture",
        name: "家具管理",
        icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 9V6a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v3"/><path d="M2 11v5a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-5a2 2 0 0 0-4 0v2H6v-2a2 2 0 0 0-4 0z"/><path d="M4 18v2"/><path d="M20 18v2"/></svg>',
        badge: 0,
      },
      {
        path: "/admin/furniture_type",
        name: "分类管理",
        icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>',
        badge: 0,
      },
    ],
  },
  {
    title: "交易管理",
    items: [
      {
        path: "/admin/orders",
        name: "订单管理",
        icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1" ry="1"/></svg>',
        badge: 0,
      },
      {
        path: "/admin/after-sale",
        name: "售后处理",
        icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><polyline points="3 3 3 8 8 8"/></svg>',
        badge: 0,
      },
    ],
  },
  {
    title: "用户与通知",
    items: [
      {
        path: "/admin/users",
        name: "用户管理",
        icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>',
        badge: 0,
      },
      {
        path: "/admin/notification",
        name: "通知管理",
        icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>',
        badge: 0,
      },
      {
        path: "/admin/notify-setting",
        name: "邮件配置",
        icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>',
        badge: 0,
      },
    ],
  },
  {
    title: "内容管理",
    items: [
      {
        path: "/admin/site-content",
        name: "首页内容",
        icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="4 17 10 11 14 15 20 9"/><polyline points="14 9 20 9 20 15"/><line x1="4" y1="21" x2="20" y2="21"/></svg>',
        badge: 0,
      },
      {
        path: "/admin/comments",
        name: "评价审核",
        icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>',
        badge: 0,
      },
    ],
  },
]);

// 分组折叠状态：默认展开，进入页面自动展开当前路由所在组
const expandedGroups = ref({});
const toggleGroup = (title) => {
  expandedGroups.value[title] = !expandedGroups.value[title];
};

let countTimer = null;

// 在分组结构中查找菜单项并设置角标
const setMenuBadge = (path, count) => {
  for (const group of menuGroups.value) {
    const menu = group.items.find((m) => m.path === path);
    if (menu) {
      menu.badge = count;
      return;
    }
  }
};

const fetchPendingCounts = async () => {
  try {
    const [orderRes, commentRes, refundRes] = await Promise.all([
      getPendingOrderCount(),
      getPendingCommentCount(),
      getPendingRefundCount(),
    ]);
    if (orderRes.success || orderRes.code === 200) {
      setMenuBadge("/admin/orders", orderRes.data?.pendingShipCount || 0);
    }
    if (commentRes.success || commentRes.code === 200) {
      const d = commentRes.data || {};
      setMenuBadge(
        "/admin/comments",
        (d.commentCount || 0) +
          (d.appendCount || 0) +
          (d.reviewCommentCount || 0),
      );
    }
    if (refundRes.success || refundRes.code === 200) {
      setMenuBadge("/admin/after-sale", refundRes.data?.pendingRefundCount || 0);
    }
  } catch (e) {
    /* ignore */
  }
};

// 自动展开当前路由所在分组
const expandCurrentGroup = () => {
  const path = router.currentRoute.value.path;
  for (const group of menuGroups.value) {
    if (!group.title) continue;
    if (group.items.some((m) => m.path === path)) {
      expandedGroups.value[group.title] = true;
    } else if (expandedGroups.value[group.title] === undefined) {
      expandedGroups.value[group.title] = true; // 默认展开
    }
  }
};

onMounted(() => {
  sys.load();
  expandCurrentGroup();
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
  gap: var(--space-3);
}
.admin-name {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  margin-right: var(--space-2);
}

/* 顶部操作按钮：轻量描边按钮，不喧宾夺主 */
.header-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  font-size: 13px;
  color: #5a6a7a;
  background: #fff;
  border: 1px solid #d8dfe6;
  border-radius: 8px;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s ease;
}
.header-action-btn:hover {
  border-color: #b8c4cf;
  background: #f5f7f9;
  color: #3a4a5a;
}

/* 退出按钮：hover 时微微泛红提示 */
.logout-btn:hover {
  border-color: #e3c4c4;
  background: #fdf6f6;
  color: var(--color-danger);
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

/* ===== 菜单分组 ===== */
.menu-group-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: var(--space-3) var(--space-2) var(--space-1);
  padding: var(--space-1) var(--space-2);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-secondary);
  cursor: pointer;
  user-select: none;
}
.menu-group-title:hover {
  color: var(--color-text-secondary);
}
.menu-group-title .group-caret {
  font-size: 10px;
  transition: transform var(--transition-fast);
  display: inline-block;
}
.menu-group-title.open .group-caret {
  transform: rotate(90deg);
}
.menu-group-items {
  display: grid;
  grid-template-rows: 0fr;
  transition: grid-template-rows 0.3s ease;
}
.menu-group-items.open {
  grid-template-rows: 1fr;
}
.menu-group-inner {
  overflow: hidden;
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
  background: #e8edf2;
  color: #3a4a5a;
  font-weight: 600;
}
.menu-item.active .menu-badge {
  background: var(--color-accent);
}
.menu-item.active .menu-icon {
  color: #5a6a7a;
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

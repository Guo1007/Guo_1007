<template>
  <header class="app-header" :class="{ 'is-scrolled': scrolled }">
    <div class="header-inner">
      <!-- Logo -->
      <router-link to="/" class="header-logo">
        <img
          v-if="sys.systemLogo"
          :src="imgUrl(sys.systemLogo)"
          class="logo-img"
          alt=""
        />
        <span v-else class="logo-mark">木</span>
        <div class="logo-text">
          <span class="logo-name">{{ sys.systemName }}</span>
          <span class="logo-tagline">{{ sys.systemTagline }}</span>
        </div>
      </router-link>

      <!-- Navigation -->
      <nav class="header-nav" ref="navRef">
        <router-link
          to="/"
          class="nav-link"
          :class="{ active: $route.path === '/' }"
        >
          首页
        </router-link>
        <div class="nav-link has-mega">
          <span class="nav-label">全部商品</span>
          <svg width="12" height="12" viewBox="0 0 12 12" class="nav-chevron">
            <path
              d="M3 4.5l3 3 3-3"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <!-- Mega Menu -->
          <div class="mega-menu">
            <div class="mega-inner">
              <div class="mega-categories">
                <router-link
                  v-for="cat in categories"
                  :key="cat.id"
                  :to="`/type/${cat.id}`"
                  class="mega-cat-item"
                >
                  <span class="mega-cat-icon">
                    <img
                      v-if="isImgPath(cat.icon)"
                      :src="imgUrl(cat.icon)"
                      class="mega-cat-img"
                    />
                    <span v-else>{{ cat.icon || "🪑" }}</span>
                  </span>
                  <span class="mega-cat-name">{{ cat.name }}</span>
                </router-link>
              </div>
            </div>
          </div>
        </div>
        <router-link
          to="/about"
          class="nav-link"
          :class="{ active: $route.path === '/about' }"
        >
          品牌故事
        </router-link>
      </nav>

      <!-- Right actions -->
      <div class="header-actions">
        <!-- Search -->
        <div class="header-search" :class="{ 'is-open': searchOpen }">
          <button class="icon-btn" @click="toggleSearch" aria-label="搜索">
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
            >
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
            </svg>
          </button>
          <Transition name="search-slide">
            <form
              v-if="searchOpen"
              class="search-form"
              @submit.prevent="doSearch"
            >
              <input
                ref="searchInput"
                v-model="searchText"
                type="text"
                placeholder="搜索家具..."
                @keydown.escape="searchOpen = false"
              />
            </form>
          </Transition>
        </div>

        <!-- Notifications -->
        <NotificationBell />

        <!-- Favorites -->
        <button
          class="icon-btn"
          @click="$router.push('/user/favorites')"
          aria-label="收藏"
        >
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path
              d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"
            />
          </svg>
        </button>

        <!-- Cart -->
        <button
          class="icon-btn cart-btn"
          @click="cartStore.openCart()"
          aria-label="购物车"
        >
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <circle cx="9" cy="21" r="1" />
            <circle cx="20" cy="21" r="1" />
            <path
              d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"
            />
          </svg>
          <span class="cart-badge" v-if="cartStore.totalCount > 0">{{
            cartStore.totalCount
          }}</span>
        </button>

        <!-- User dropdown -->
        <div class="user-menu" @click="toggleUserMenu" ref="userMenuRef">
          <div class="user-avatar-sm">
            <img
              :src="userAvatar"
              alt=""
              @error="(e) => (e.target.src = '/images/default-avatar.png')"
            />
          </div>
          <Transition name="menu-fade">
            <div class="user-dropdown" v-if="userMenuOpen" @click.stop>
              <div class="dropdown-hd">
                <img
                  :src="userAvatar"
                  alt=""
                  class="dropdown-avatar"
                  @error="(e) => (e.target.src = '/images/default-avatar.png')"
                />
                <div>
                  <p class="dropdown-name">{{ userName }}</p>
                  <p class="dropdown-role">尊享会员</p>
                </div>
              </div>
              <div class="dropdown-divider"></div>
              <router-link
                to="/user/profile"
                class="dropdown-item"
                @click="userMenuOpen = false"
              >
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
                个人中心
              </router-link>
              <router-link
                to="/user/orders"
                class="dropdown-item"
                @click="userMenuOpen = false"
              >
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <rect x="1" y="3" width="15" height="13" rx="2" />
                  <polygon points="23 7 16 12 23 17 23 7" />
                </svg>
                我的订单
              </router-link>
              <router-link
                to="/user/addresses"
                class="dropdown-item"
                @click="userMenuOpen = false"
              >
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                  <circle cx="12" cy="10" r="3" />
                </svg>
                收货地址
              </router-link>
              <div class="dropdown-divider" v-if="isAdmin"></div>
              <router-link
                v-if="isAdmin"
                to="/admin"
                class="dropdown-item admin-entry"
                @click="userMenuOpen = false"
              >
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <rect x="2" y="3" width="20" height="14" rx="2" />
                  <line x1="8" y1="21" x2="16" y2="21" />
                  <line x1="12" y1="17" x2="12" y2="21" />
                </svg>
                后台管理
              </router-link>
              <div class="dropdown-divider"></div>
              <button class="dropdown-item logout" @click="handleLogout">
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                  <polyline points="16 17 21 12 16 7" />
                  <line x1="21" y1="12" x2="9" y2="12" />
                </svg>
                退出登录
              </button>
            </div>
          </Transition>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  watch,
} from "vue";
import { useRouter, useRoute } from "vue-router";
import { useCartStore } from "@/stores/cart.js";
import { useSystemStore } from "@/stores/system.js";
import { useLogout } from "@/composables/useLogout.js";
import { getFurnitureTypeList } from "@/api/furniture.js";
import { imgUrl } from "@/utils/img.js";
import NotificationBell from "@/components/NotificationBell.vue";

const router = useRouter();
const route = useRoute();
const cartStore = useCartStore();
const sys = useSystemStore();
const { logout } = useLogout();

const scrolled = ref(false);
const searchOpen = ref(false);
const searchText = ref("");
const searchInput = ref(null);
const userMenuOpen = ref(false);
const userMenuRef = ref(null);

const userName = ref("用户");
const userAvatar = ref("/images/default-avatar.png");
const categories = ref([]);

const isAdmin = computed(() => {
  try {
    const info = JSON.parse(localStorage.getItem("userInfo") || "{}");
    return info.isAdmin === 1;
  } catch {
    return false;
  }
});

const isImgPath = (str) =>
  str && (str.startsWith("/") || str.startsWith("http"));

const handleLogout = () => {
  userMenuOpen.value = false;
  logout();
};

// Scroll detection
const onScroll = () => {
  scrolled.value = window.scrollY > 10;
};

// User info
const loadUser = () => {
  try {
    const info = JSON.parse(localStorage.getItem("userInfo") || "{}");
    userName.value = info.userName || "用户";
    userAvatar.value = imgUrl(info.icon, "/images/default-avatar.png");
  } catch {
    /* ignore */
  }
};

// Categories
const loadCategories = async () => {
  try {
    const res = await getFurnitureTypeList();
    if (res.success && Array.isArray(res.data))
      categories.value = res.data.slice(0, 8);
  } catch {
    /* ignore */
  }
};

// Search
const toggleSearch = () => {
  searchOpen.value = !searchOpen.value;
  if (searchOpen.value) nextTick(() => searchInput.value?.focus());
};
const doSearch = () => {
  const q = searchText.value.trim();
  if (q) router.push({ path: "/type/0", query: { keyword: q } });
  searchOpen.value = false;
  searchText.value = "";
};

// Route change close
watch(
  () => route.path,
  () => {
    userMenuOpen.value = false;
    searchOpen.value = false;
  },
);
const closeUserMenu = (e) => {
  if (userMenuRef.value && !userMenuRef.value.contains(e.target))
    userMenuOpen.value = false;
};
const toggleUserMenu = () => {
  userMenuOpen.value = !userMenuOpen.value;
};

// Route change close

onMounted(() => {
  window.addEventListener("scroll", onScroll, { passive: true });
  document.addEventListener("click", closeUserMenu);
  sys.load();
  loadUser();
  loadCategories();
});
onBeforeUnmount(() => {
  window.removeEventListener("scroll", onScroll);
  document.removeEventListener("click", closeUserMenu);
});
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: var(--color-surface);
  border-bottom: 1px solid transparent;
  transition:
    border-color var(--transition-normal),
    box-shadow var(--transition-normal);
}
.app-header.is-scrolled {
  border-bottom-color: var(--color-border-light);
  box-shadow: var(--shadow-sm);
}

.header-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--space-6);
  height: var(--header-height);
  display: flex;
  align-items: center;
  gap: var(--space-8);
}

/* Logo */
.header-logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
  text-decoration: none;
}
.logo-mark {
  width: 36px;
  height: 36px;
  background: var(--color-dark);
  color: var(--color-text-inverse);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  border-radius: var(--radius-md);
  font-family: var(--font-serif);
}
.logo-img {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  object-fit: contain;
}
.logo-text {
  display: flex;
  flex-direction: column;
}
.logo-name {
  font-size: var(--text-md);
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.08em;
  line-height: 1.2;
}
.logo-tagline {
  font-size: 10px;
  color: var(--color-text-tertiary);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

/* Navigation */
.header-nav {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  flex: 1;
}
.nav-link {
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  text-decoration: none;
  border-radius: var(--radius-md);
  transition:
    color var(--transition-fast),
    background var(--transition-fast);
  display: flex;
  align-items: center;
  gap: var(--space-1);
  cursor: pointer;
  white-space: nowrap;
}
.nav-link:hover,
.nav-link.active {
  color: var(--color-text-primary);
  background: var(--color-bg);
}
.nav-label {
  font-size: var(--text-sm);
}
.nav-chevron {
  transition: transform var(--transition-fast);
}
.has-mega:hover .nav-chevron {
  transform: rotate(180deg);
}

/* Mega Menu */
.has-mega {
  position: relative;
}
.has-mega .mega-menu {
  display: none;
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  padding-top: 8px;
  z-index: 1001;
}
.has-mega:hover .mega-menu {
  display: block;
}
.mega-inner {
  padding: var(--space-6);
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  min-width: 480px;
}
.mega-categories {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-2);
}
.mega-cat-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: all var(--transition-fast);
}
.mega-cat-item:hover {
  background: var(--color-bg);
  color: var(--color-text-primary);
}
.mega-cat-icon {
  font-size: 18px;
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.mega-cat-img {
  width: 24px;
  height: 24px;
  object-fit: contain;
  border-radius: 3px;
}
.mega-cat-name {
  white-space: nowrap;
}

/* Actions */
.header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  flex-shrink: 0;
}

.icon-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
  position: relative;
}
.icon-btn:hover {
  color: var(--color-text-primary);
  background: var(--color-bg);
}

/* Cart badge */
.cart-btn {
  position: relative;
}
.cart-badge {
  position: absolute;
  top: 4px;
  right: 4px;
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
  line-height: 1;
}

/* Search */
.header-search {
  position: relative;
  display: flex;
  align-items: center;
}
.search-form {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
}
.search-form input {
  width: 240px;
  padding: var(--space-2) var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  outline: none;
  background: var(--color-surface);
  color: var(--color-text-primary);
  transition: border-color var(--transition-fast);
}
.search-form input:focus {
  border-color: var(--color-dark);
}
.search-form input::placeholder {
  color: var(--color-text-tertiary);
}

/* User menu */
.user-menu {
  position: relative;
}
.user-avatar-sm {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color var(--transition-fast);
}
.user-avatar-sm:hover {
  border-color: var(--color-border);
}
.user-avatar-sm img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  width: 220px;
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: var(--space-2);
  z-index: 1001;
}
.dropdown-hd {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
}
.dropdown-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
}
.dropdown-name {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}
.dropdown-role {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  margin-top: 2px;
}
.dropdown-divider {
  height: 1px;
  background: var(--color-border-light);
  margin: var(--space-1) 0;
}
.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  text-decoration: none;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  cursor: pointer;
  width: 100%;
}
.dropdown-item:hover {
  background: var(--color-bg);
  color: var(--color-text-primary);
}
.dropdown-item.admin-entry {
  color: var(--color-accent);
}
.dropdown-item.admin-entry:hover {
  background: var(--color-accent-subtle);
}
.dropdown-item.logout {
  color: var(--color-text-tertiary);
}
.dropdown-item.logout:hover {
  color: var(--color-danger);
  background: #fef5f5;
}

/* Transitions */

.search-slide-enter-active,
.search-slide-leave-active {
  transition:
    opacity 0.15s ease,
    transform 0.15s ease;
}
.search-slide-enter-from,
.search-slide-leave-to {
  opacity: 0;
  transform: translateY(-50%) scale(0.95);
}

.menu-fade-enter-active,
.menu-fade-leave-active {
  transition:
    opacity 0.15s ease,
    transform 0.15s ease;
}
.menu-fade-enter-from,
.menu-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

@media (max-width: 768px) {
  .header-nav {
    display: none;
  }
  .logo-tagline {
    display: none;
  }
  .header-inner {
    gap: var(--space-4);
    padding: 0 var(--space-4);
  }
  .search-form input {
    width: 160px;
  }
}
</style>

<template>
  <div class="cart-page">
    <div class="cart-container">
      <!-- Breadcrumb -->
      <div class="cart-breadcrumb">
        <button class="breadcrumb-back" @click="goBack" title="返回">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        </button>
        <router-link to="/">首页</router-link>
        <span>/</span>
        <span class="current">购物车</span>
      </div>

      <h1 class="cart-title">
        我的购物车<span class="cart-count" v-if="!cartStore.isEmpty"
          >（{{ cartStore.totalCount }} 件）</span
        >
      </h1>

      <!-- Empty -->
      <div v-if="cartStore.isEmpty" class="cart-empty-state">
        <span class="empty-icon">🛒</span>
        <h2>购物车是空的</h2>
        <p>去挑选一些心仪的家具吧</p>
        <router-link to="/type/0" class="shop-btn">去逛逛</router-link>
      </div>

      <!-- Cart content -->
      <div v-else class="cart-layout">
        <!-- Main -->
        <div class="cart-main">
          <!-- Select all -->
          <div class="cart-select-all">
            <label class="checkbox-label">
              <input
                type="checkbox"
                :checked="allSelected"
                @change="toggleAll"
              />
              <span class="checkmark"></span>
              <span>全选</span>
            </label>
            <button class="clear-btn" @click="cartStore.clearCart">
              清空购物车
            </button>
          </div>

          <!-- Items -->
          <div class="cart-items">
            <div
              class="cart-item"
              v-for="item in cartStore.items"
              :key="item.cartItemId"
            >
              <label class="checkbox-label">
                <input
                  type="checkbox"
                  v-model="selectedIds"
                  :value="item.cartItemId"
                />
                <span class="checkmark"></span>
              </label>
              <img
                :src="imgUrl(item.fIcon, '/images/default-furniture.png')"
                class="item-img"
                @click="goDetail(item.id)"
                @error="handleImgError"
              />
              <div class="item-info">
                <h4 class="item-name" @click="goDetail(item.id)">
                  {{ item.fName }}
                </h4>
                <p class="item-spec" v-if="item.specText">
                  {{ item.specText }}
                </p>
              </div>
              <div class="item-price">¥{{ formatPrice(item.price) }}</div>
              <div class="item-qty">
                <button
                  class="qty-btn"
                  @click="cartStore.decreaseQuantity(item.cartItemId)"
                >
                  −
                </button>
                <span class="qty-val">{{ item.quantity }}</span>
                <button
                  class="qty-btn"
                  @click="cartStore.increaseQuantity(item.cartItemId)"
                  :disabled="item.quantity >= item.stock"
                >
                  +
                </button>
              </div>
              <div class="item-subtotal">
                ¥{{ formatPrice(item.price * item.quantity) }}
              </div>
              <button
                class="item-remove"
                @click="cartStore.removeItem(item.cartItemId)"
                title="删除"
              >
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <aside class="cart-sidebar">
          <div class="sidebar-card">
            <h3 class="sidebar-title">订单摘要</h3>
            <div class="summary-row">
              <span>商品数量</span>
              <span>{{ selectedCount }} 件</span>
            </div>
            <div class="summary-row total">
              <span>合计</span>
              <span class="total-price">¥{{ selectedTotal }}</span>
            </div>
            <button
              class="checkout-btn"
              :disabled="selectedIds.length === 0"
              @click="goCheckout"
            >
              去结算
            </button>
            <router-link to="/type/0" class="continue-link"
              >继续选购</router-link
            >
          </div>

          <!-- Address preview (if available) -->
          <div class="sidebar-card address-card" v-if="defaultAddress">
            <h3 class="sidebar-title">默认收货地址</h3>
            <p class="addr-name">
              {{ defaultAddress.consignee }}
              <span class="addr-phone">{{ defaultAddress.phone }}</span>
            </p>
            <p class="addr-detail">{{ defaultAddress.address }}</p>
            <router-link to="/user/addresses" class="addr-change"
              >修改地址</router-link
            >
          </div>
        </aside>
      </div>

      <!-- Recently viewed -->
      <div class="recent-section" v-if="recentProducts.length > 0">
        <h3 class="recent-title">最近浏览</h3>
        <div class="recent-scroll">
          <ProductCard v-for="p in recentProducts" :key="p.id" :product="p" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { useCartStore } from "@/stores/cart.js";
import { getAddressList } from "@/api/address.js";
import { getFurnitureByTypeId } from "@/api/furniture.js";
import { imgUrl } from "@/utils/img.js";
import { formatPrice } from "@/utils/format.js";
import { ElMessage } from "element-plus";
import ProductCard from "@/components/product/ProductCard.vue";
import { useBackNavigation } from '@/composables/useBackNavigation.js';
import { useRequireLogin } from "@/composables/useRequireLogin.js";

const router = useRouter();
const cartStore = useCartStore();
const { goBack } = useBackNavigation();
const { requireLogin } = useRequireLogin();
const selectedIds = ref([]);
const defaultAddress = ref(null);
const recentProducts = ref([]);

const allSelected = computed({
  get: () =>
    cartStore.items.length > 0 &&
    selectedIds.value.length === cartStore.items.length,
  set: (v) => {
    selectedIds.value = v ? cartStore.items.map((i) => i.cartItemId) : [];
  },
});

const toggleAll = () => {
  allSelected.value = !allSelected.value;
};

const selectedCount = computed(() => {
  return cartStore.items
    .filter((i) => selectedIds.value.includes(i.cartItemId))
    .reduce((s, i) => s + i.quantity, 0);
});

const selectedTotal = computed(() => {
  return formatPrice(
    cartStore.items
      .filter((i) => selectedIds.value.includes(i.cartItemId))
      .reduce((s, i) => s + i.price * i.quantity, 0),
  );
});

const goDetail = (id) => router.push(`/furniture/detail/${id}`);

const goCheckout = () => {
  // 未登录引导登录
  if (!requireLogin("结算需要登录")) return;
  if (selectedIds.value.length === 0) {
    ElMessage.warning("请选择要结算的商品");
    return;
  }
  // 把勾选的商品交给抽屉结算：抽屉按选中项下单并只清空选中商品，未勾选的留在购物车
  cartStore.checkoutIds = [...selectedIds.value];
  cartStore.openCart();
};

const handleImgError = (e) => {
  e.target.src = "/images/default-furniture.png";
};

onMounted(async () => {
  selectedIds.value = cartStore.items.map((i) => i.cartItemId);

  // 已登录才加载默认地址（游客浏览购物车不触发需登录接口）
  if (localStorage.getItem("token")) {
    try {
      const res = await getAddressList();
      if ((res.success || res.code === 200) && Array.isArray(res.data)) {
        defaultAddress.value =
          res.data.find((a) => a.isDefault === 1) || res.data[0] || null;
      }
    } catch {
      /* ignore */
    }
  }

  // Recent products
  try {
    const res = await getFurnitureByTypeId({ typeId: 0, current: 1, size: 4 });
    if ((res.success || res.code === 200) && res.data) {
      recentProducts.value = res.data.records || [];
    }
  } catch {
    /* ignore */
  }
});
</script>

<style scoped lang="scss">
@import "@/styles/views/cart-view.scss";
</style>

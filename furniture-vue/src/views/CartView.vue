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

const router = useRouter();
const cartStore = useCartStore();
const { goBack } = useBackNavigation();
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
  if (selectedIds.value.length === 0) {
    ElMessage.warning("请选择要结算的商品");
    return;
  }
  // Filter cart items to only selected, then create order via drawer's checkout logic
  const selectedItems = cartStore.items.filter((i) =>
    selectedIds.value.includes(i.cartItemId),
  );
  // Navigate to first item's buy dialog won't work for cart. Open cart drawer instead.
  cartStore.openCart();
  router.push("/");
};

const handleImgError = (e) => {
  e.target.src = "/images/default-furniture.png";
};

onMounted(async () => {
  selectedIds.value = cartStore.items.map((i) => i.cartItemId);

  try {
    const res = await getAddressList();
    if ((res.success || res.code === 200) && Array.isArray(res.data)) {
      defaultAddress.value =
        res.data.find((a) => a.isDefault === 1) || res.data[0] || null;
    }
  } catch {
    /* ignore */
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

<style scoped>
.cart-page {
  min-height: 60vh;
  background: var(--color-bg);
}
.cart-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
}

/* Breadcrumb */
.cart-breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  margin-bottom: var(--space-4);
}
.cart-breadcrumb a {
  text-decoration: none;
  color: var(--color-text-tertiary);
}
.cart-breadcrumb a:hover {
  color: var(--color-text-primary);
}
.current {
  color: var(--color-text-primary);
}

.breadcrumb-back {
  display: flex; align-items: center; justify-content: center;
  width: 26px; height: 26px; border-radius: 50%;
  border: none; background: transparent;
  color: var(--color-text-tertiary);
  transition: all var(--transition-fast);
  margin-right: var(--space-2); flex-shrink: 0; cursor: pointer;
}
.breadcrumb-back:hover {
  background: var(--color-border-light);
  color: var(--color-text-primary);
}

.cart-title {
  font-size: var(--text-2xl);
  font-weight: 700;
  font-family: var(--font-serif);
  color: var(--color-text-primary);
  margin-bottom: var(--space-8);
}
.cart-count {
  font-weight: 400;
  color: var(--color-text-tertiary);
  font-size: var(--text-md);
}

/* Empty */
.cart-empty-state {
  text-align: center;
  padding: var(--space-16) 0;
}
.empty-icon {
  font-size: 64px;
  display: block;
  margin-bottom: var(--space-4);
}
.cart-empty-state h2 {
  font-size: var(--text-xl);
  font-weight: 600;
  margin-bottom: var(--space-2);
}
.cart-empty-state p {
  color: var(--color-text-tertiary);
  margin-bottom: var(--space-6);
}
.shop-btn {
  display: inline-block;
  padding: var(--space-3) var(--space-8);
  background: var(--color-dark);
  color: #fff;
  border-radius: var(--radius-md);
  text-decoration: none;
  font-weight: 600;
  font-size: var(--text-sm);
}

/* Layout */
.cart-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: var(--space-6);
  align-items: start;
}

/* Main */
.cart-main {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
}

/* Select all */
.cart-select-all {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--color-border-light);
}
.clear-btn {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  cursor: pointer;
  transition: color var(--transition-fast);
}
.clear-btn:hover {
  color: var(--color-danger);
}

/* Checkbox */
.checkbox-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  user-select: none;
}
.checkbox-label input {
  display: none;
}
.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid var(--color-border);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}
.checkbox-label input:checked + .checkmark {
  background: var(--color-dark);
  border-color: var(--color-dark);
}
.checkbox-label input:checked + .checkmark::after {
  content: "";
  width: 5px;
  height: 9px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
  margin-top: -2px;
}

/* Cart items */
.cart-items {
  padding: 0 var(--space-5);
}
.cart-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5) 0;
  border-bottom: 1px solid var(--color-border-light);
}
.cart-item:last-child {
  border-bottom: none;
}

.item-img {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-md);
  object-fit: cover;
  cursor: pointer;
  background: var(--color-bg);
  flex-shrink: 0;
}
.item-info {
  flex: 1;
  min-width: 0;
}
.item-name {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-primary);
  cursor: pointer;
  margin-bottom: var(--space-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-name:hover {
  color: var(--color-accent);
}
.item-spec {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  background: var(--color-bg);
  display: inline-block;
  padding: 1px 8px;
  border-radius: var(--radius-sm);
}
.item-price {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  flex-shrink: 0;
  width: 80px;
  text-align: center;
}

/* Qty */
.item-qty {
  display: flex;
  align-items: center;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}
.qty-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
}
.qty-btn:hover:not(:disabled) {
  background: var(--color-bg);
  color: var(--color-text-primary);
}
.qty-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}
.qty-val {
  min-width: 36px;
  text-align: center;
  font-size: var(--text-sm);
  font-weight: 600;
}

.item-subtotal {
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--color-accent);
  flex-shrink: 0;
  width: 90px;
  text-align: right;
}
.item-remove {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-tertiary);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}
.item-remove:hover {
  color: var(--color-danger);
  background: #fef5f5;
}

/* Sidebar */
.cart-sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
.sidebar-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
}
.sidebar-title {
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--color-border-light);
}
.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--space-2);
}
.summary-row.total {
  font-weight: 700;
  color: var(--color-text-primary);
  font-size: var(--text-md);
  margin-top: var(--space-3);
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-border-light);
}
.total-price {
  color: var(--color-accent);
  font-size: var(--text-lg);
}

.checkout-btn {
  width: 100%;
  padding: var(--space-3);
  background: var(--color-dark);
  color: #fff;
  font-size: var(--text-sm);
  font-weight: 600;
  border-radius: var(--radius-md);
  margin-top: var(--space-4);
  cursor: pointer;
  transition: background var(--transition-fast);
  border: none;
}
.checkout-btn:hover:not(:disabled) {
  background: var(--color-dark-hover);
}
.checkout-btn:disabled {
  background: var(--color-border);
  cursor: not-allowed;
}

.continue-link {
  display: block;
  text-align: center;
  margin-top: var(--space-3);
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  text-decoration: none;
}
.continue-link:hover {
  color: var(--color-text-primary);
}

/* Address card */
.address-card {
  font-size: var(--text-sm);
}
.addr-name {
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--space-1);
}
.addr-phone {
  font-weight: 400;
  color: var(--color-text-secondary);
  font-size: var(--text-xs);
}
.addr-detail {
  color: var(--color-text-tertiary);
  font-size: var(--text-xs);
}
.addr-change {
  display: inline-block;
  margin-top: var(--space-3);
  font-size: var(--text-xs);
  color: var(--color-accent);
  text-decoration: none;
}

/* Recent */
.recent-section {
  margin-top: var(--space-12);
}
.recent-title {
  font-size: var(--text-lg);
  font-weight: 700;
  font-family: var(--font-serif);
  color: var(--color-text-primary);
  margin-bottom: var(--space-5);
}
.recent-scroll {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}

@media (max-width: 768px) {
  .cart-layout {
    grid-template-columns: 1fr;
  }
  .item-price,
  .item-subtotal {
    width: auto;
    font-size: var(--text-xs);
  }
  .cart-item {
    flex-wrap: wrap;
    gap: var(--space-2);
  }
  .recent-scroll {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

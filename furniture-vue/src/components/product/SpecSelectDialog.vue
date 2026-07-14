<template>
  <el-dialog
    v-model="visible"
    title="选择规格"
    width="420px"
    :close-on-click-modal="false"
    destroy-on-close
    class="spec-dialog"
  >
    <div class="dialog-body">
      <!-- 商品摘要 -->
      <div class="product-summary">
        <div class="ps-img-wrap">
          <img
            v-if="product.fIcon"
            :src="imgUrl(product.fIcon)"
            :alt="product.fName"
            class="ps-img"
          />
          <span v-else class="ps-emoji">🪑</span>
        </div>
        <div class="ps-info">
          <p class="ps-name">{{ product.fName }}</p>
          <p class="ps-price">¥{{ formatPrice(displayPrice) }}</p>
        </div>
      </div>

      <!-- 规格选择 -->
      <div class="spec-section" v-if="specGroups.length > 0">
        <div class="spec-group" v-for="group in specGroups" :key="group.id">
          <div class="spec-group-label">{{ group.groupName }}</div>
          <div class="spec-values">
            <div
              v-for="val in group.values"
              :key="val.id"
              class="spec-value-item"
              :class="{
                active: selectedSpecs[group.groupName] === val.valueName,
                disabled: !isSpecValueAvailable(skuList, group.groupName, val.valueName),
              }"
              @click="onSelectSpec(group.groupName, val.valueName)"
            >
              <span>{{ val.valueName }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 已选规格提示 -->
      <div class="spec-selected-info" v-if="selectedSku">
        已选：{{ selectedSku.specText }}
      </div>

      <!-- 库存提示 -->
      <div class="stock-info" v-if="specGroups.length > 0 && !selectedSku">
        <span class="stock-hint">请选择完整规格</span>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button
        type="primary"
        :disabled="specGroups.length > 0 && !selectedSku"
        @click="handleConfirm"
      >
        加入购物车
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, watch } from 'vue'
import { imgUrl } from '@/utils/img.js'
import { formatPrice } from '@/utils/format.js'
import { useSpecSelection } from '@/composables/useSpecSelection.js'

const props = defineProps({
  visible: Boolean,
  product: { type: Object, default: () => ({}) },
  specGroups: { type: Array, default: () => [] },
  skuList: { type: Array, default: () => [] },
})

const emit = defineEmits(['update:visible', 'confirm'])

const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val),
})

const {
  selectedSpecs,
  selectedSku,
  selectSpec,
  isSpecValueAvailable,
  resetSelection,
} = useSpecSelection()

// 当弹窗打开时重置选择状态
watch(() => props.visible, (val) => {
  if (val) resetSelection()
})

// 当前显示价格（选中 SKU 时用 SKU 价格）
const displayPrice = computed(() => {
  if (selectedSku.value) return selectedSku.value.price
  return props.product.price || 0
})

const onSelectSpec = (groupName, valueName) => {
  selectSpec(props.specGroups, props.skuList, groupName, valueName)
}

const handleCancel = () => {
  visible.value = false
}

const handleConfirm = () => {
  const sku = selectedSku.value
  if (!sku) return
  emit('confirm', {
    skuId: sku.id,
    price: sku.price,
    stock: sku.stock,
    skuImage: sku.skuImage,
    specText: sku.specText || '',
  })
}
</script>

<style scoped>
.spec-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid var(--color-border-light);
  padding: var(--space-4) var(--space-5);
}
.spec-dialog :deep(.el-dialog__title) {
  font-size: var(--text-md);
  font-weight: 600;
  color: var(--color-text-primary);
}
.spec-dialog :deep(.el-dialog__footer) {
  border-top: 1px solid var(--color-border-light);
  padding: var(--space-3) var(--space-5);
}
.spec-dialog :deep(.el-button--primary) {
  background: var(--color-dark);
  border-color: var(--color-dark);
}
.spec-dialog :deep(.el-button--primary:hover) {
  background: var(--color-dark-hover);
  border-color: var(--color-dark-hover);
}

.dialog-body {
  padding: 0;
}

/* 商品摘要 */
.product-summary {
  display: flex;
  gap: var(--space-4);
  padding-bottom: var(--space-4);
  margin-bottom: var(--space-4);
  border-bottom: 1px solid var(--color-border-light);
}
.ps-img-wrap {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-md);
  background: var(--color-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}
.ps-img { width: 100%; height: 100%; object-fit: cover; }
.ps-emoji { font-size: 32px; }
.ps-info { flex: 1; display: flex; flex-direction: column; justify-content: center; }
.ps-name {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--space-1);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.ps-price {
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-accent);
}

/* 规格区域 */
.spec-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
.spec-group-label {
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--color-text-secondary);
  margin-bottom: var(--space-2);
}
.spec-values {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}
.spec-value-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: var(--text-sm);
  color: var(--color-text-primary);
  transition: all var(--transition-fast);
  background: var(--color-surface);
  user-select: none;
}
.spec-value-item:hover:not(.disabled) {
  border-color: var(--color-dark);
  color: var(--color-dark);
}
.spec-value-item.active {
  border-color: var(--color-dark);
  background: var(--color-dark);
  color: #fff;
  font-weight: 500;
}
.spec-value-item.disabled {
  border-color: var(--color-border-light);
  color: var(--color-text-tertiary);
  cursor: not-allowed;
  background: var(--color-bg);
  pointer-events: none;
}

/* 选中信息和库存提示 */
.spec-selected-info {
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  margin-top: var(--space-4);
  padding: var(--space-2) var(--space-3);
  background: var(--color-bg);
  border-radius: var(--radius-sm);
}
.stock-info {
  margin-top: var(--space-4);
}
.stock-hint {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
}
</style>

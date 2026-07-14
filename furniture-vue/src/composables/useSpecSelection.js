import { ref } from 'vue'

/**
 * 解包 ref 或普通数组 — 兼容 useFurniture.js（传 ref）和 SpecSelectDialog（传 props 普通数组）
 */
const unwrap = (v) => (v && 'value' in v) ? v.value : v

/**
 * 可复用的规格选择逻辑
 * 从 useFurniture.js 中提取，供详情页和快速加购弹窗共用
 */
export function useSpecSelection() {
  const selectedSpecs = ref({})
  const selectedSku = ref(null)

  // 选择/取消规格值
  const selectSpec = (specGroups, skuList, groupName, valueName) => {
    if (selectedSpecs.value[groupName] === valueName) {
      delete selectedSpecs.value[groupName]
      selectedSpecs.value = { ...selectedSpecs.value }
      matchSku(specGroups, skuList)
      return
    }
    if (!isSpecValueAvailable(skuList, groupName, valueName)) return
    selectedSpecs.value[groupName] = valueName
    selectedSpecs.value = { ...selectedSpecs.value }
    matchSku(specGroups, skuList)
  }

  // 匹配 SKU
  const matchSku = (specGroups, skuList) => {
    const list = unwrap(skuList)
    const selectedCount = Object.keys(selectedSpecs.value).length
    if (selectedCount === 0 || !list.length) {
      selectedSku.value = null
      return null
    }
    const matched = list.find((sku) => {
      if (!sku.specMap) return false
      const mapKeys = Object.keys(sku.specMap)
      if (mapKeys.length !== selectedCount) return false
      return mapKeys.every(
        (key) => selectedSpecs.value[key] === sku.specMap[key]
      )
    })
    selectedSku.value = matched || null
    return selectedSku.value
  }

  // 某个规格值是否可选（对应 SKU 有库存）
  const isSpecValueAvailable = (skuList, groupName, valueName) => {
    const list = unwrap(skuList)
    return list.some((sku) => {
      if (!sku.specMap || sku.stock <= 0) return false
      if (sku.specMap[groupName] !== valueName) return false
      return Object.entries(selectedSpecs.value).every(([g, v]) => {
        if (g === groupName) return true
        return sku.specMap[g] === v
      })
    })
  }

  const resetSelection = () => {
    selectedSpecs.value = {}
    selectedSku.value = null
  }

  return {
    selectedSpecs,
    selectedSku,
    selectSpec,
    matchSku,
    isSpecValueAvailable,
    resetSelection,
  }
}

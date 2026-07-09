import {computed, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {getFurnitureById, getFurnitureByTypeId, getFurnitureSpecs} from '@/api/furniture.js'
import {createOrder} from '@/api/order.js'
import {useCartStore} from '@/stores/cart.js'
import {formatPrice} from '@/utils/format.js'
import {logger} from '@/utils/logger.js'

const cartStore = useCartStore()

export function useFurnitureDetail() {
    const router = useRouter()

    const furniture = ref({})
    const loading = ref(true)
    const quantity = ref(1)

    // ========== 规格相关 ==========
    const specGroups = ref([])
    const skuList = ref([])
    const selectedSpecs = ref({})  // { groupName: valueName }
    const selectedSku = ref(null)
    const specLoading = ref(false)

    // 是否有多规格
    const hasSpecs = computed(() => specGroups.value.length > 0)

    // 当前显示的价格（选中SKU时用SKU价格，否则用商品价格）
    const displayPrice = computed(() => {
        if (selectedSku.value) return selectedSku.value.price
        return furniture.value.price
    })

    // 当前显示的库存
    const displayStock = computed(() => {
        if (selectedSku.value) return selectedSku.value.stock
        return furniture.value.stock
    })

    // 当前显示的图片（选中SKU有图片时用SKU图片）
    const displayImage = computed(() => {
        if (selectedSku.value && selectedSku.value.skuImage) return selectedSku.value.skuImage
        return null
    })

    // 加载商品规格
    const loadSpecs = async (furnitureId) => {
        specLoading.value = true
        try {
            const res = await getFurnitureSpecs(furnitureId)
            if ((res.success || res.code === 200) && res.data) {
                specGroups.value = res.data.specGroups || []
                skuList.value = res.data.skuList || []
                // 初始化选中状态
                selectedSpecs.value = {}
                selectedSku.value = null
                // 如果只有一个SKU且无规格，直接选中
                if (specGroups.value.length === 0 && skuList.value.length === 1) {
                    selectedSku.value = skuList.value[0]
                }
            }
        } catch (e) {
            logger.error('加载规格失败:', e)
        } finally {
            specLoading.value = false
        }
    }

    // 选择规格值
    const selectSpec = (groupName, valueName) => {
        if (selectedSpecs.value[groupName] === valueName) {
            // 取消选中（已选中的始终可以取消）
            delete selectedSpecs.value[groupName]
            selectedSpecs.value = {...selectedSpecs.value}
            matchSku()
            return
        }
        // 不可用的规格值禁止选中
        if (!isSpecValueAvailable(groupName, valueName)) {
            return
        }
        selectedSpecs.value[groupName] = valueName
        // 触发响应式更新
        selectedSpecs.value = {...selectedSpecs.value}
        matchSku()
    }

    // 匹配SKU
    const matchSku = () => {
        const selectedCount = Object.keys(selectedSpecs.value).length
        const groupCount = specGroups.value.length

        if (selectedCount === 0) {
            selectedSku.value = null
            return
        }

        // 查找完全匹配的SKU
        const matched = skuList.value.find(sku => {
            if (!sku.specMap) return false
            const mapKeys = Object.keys(sku.specMap)
            if (mapKeys.length !== selectedCount) return false
            return mapKeys.every(key =>
                selectedSpecs.value[key] === sku.specMap[key]
            )
        })

        if (matched) {
            selectedSku.value = matched
            // 重置数量不超过库存
            if (quantity.value > matched.stock) {
                quantity.value = Math.max(1, matched.stock)
            }
        } else {
            selectedSku.value = null
        }
    }

    // 获取某规格值是否可选（对应的SKU是否有库存）
    const isSpecValueAvailable = (groupName, valueName) => {
        return skuList.value.some(sku => {
            if (!sku.specMap || sku.stock <= 0) return false
            // 检查这个SKU的该规格值是否匹配
            if (sku.specMap[groupName] !== valueName) return false
            // 检查其他已选规格是否匹配
            return Object.entries(selectedSpecs.value).every(([g, v]) => {
                if (g === groupName) return true
                return sku.specMap[g] === v
            })
        })
    }

    // 购买对话框相关
    const buyDialogVisible = ref(false)
    const buyLoading = ref(false)

    // 收货信息表单
    const buyForm = ref({
        consignee: '',
        phone: '',
        address: '',
        remark: ''
    })

    const decreaseQty = () => {
        if (quantity.value > 1) quantity.value--
    }

    const increaseQty = () => {
        if (quantity.value < displayStock.value) quantity.value++
    }

    const addToCart = () => {
        if (hasSpecs.value && !selectedSku.value) {
            ElMessage.warning('请先选择规格')
            return
        }
        const skuInfo = selectedSku.value ? {
            skuId: selectedSku.value.id,
            price: selectedSku.value.price,
            stock: selectedSku.value.stock,
            skuImage: selectedSku.value.skuImage,
            specText: selectedSku.value.specText || ''
        } : null
        cartStore.addItem(furniture.value, quantity.value, skuInfo)
    }

    const openBuyDialog = () => {
        buyForm.value = {
            consignee: '',
            phone: '',
            address: '',
            remark: ''
        }

        buyDialogVisible.value = true
    }

    // 关闭购买对话框
    const closeBuyDialog = () => {
        buyDialogVisible.value = false
        buyLoading.value = false
        // 重置表单
        buyForm.value = {
            consignee: '',
            phone: '',
            address: '',
            remark: ''
        }
    }

    // 立即购买 - 提交订单
    const submitBuy = async () => {
        // 表单验证
        if (!buyForm.value.consignee || !buyForm.value.consignee.trim()) {
            ElMessage.warning('请输入收货人姓名')
            return false
        }
        if (!buyForm.value.phone || !buyForm.value.phone.trim()) {
            ElMessage.warning('请输入联系电话')
            return false
        }
        if (!buyForm.value.address || !buyForm.value.address.trim()) {
            ElMessage.warning('请输入收货地址')
            return false
        }

        // 构建订单数据
        const orderData = {
            consignee: buyForm.value.consignee,
            phone: buyForm.value.phone,
            address: buyForm.value.address,
            remark: buyForm.value.remark,
            itemList: [
                {
                    furnitureId: furniture.value.id,
                    skuId: selectedSku.value ? selectedSku.value.id : null,
                    quantity: quantity.value
                }
            ]
        }

        buyLoading.value = true
        try {
            const res = await createOrder(orderData)
            if (res.success || res.code === 200) {
                ElMessage.success('订单创建成功！')
                closeBuyDialog()
                router.push(`/user/orders`)
                return true
            } else {
                ElMessage.error(res.msg || '订单创建失败')
                return false
            }
        } catch (error) {
            logger.error('submitBuy:', error)
            return false
        } finally {
            buyLoading.value = false
        }
    }

    // 立即购买按钮点击
    const buyNow = () => {
        if (hasSpecs.value && !selectedSku.value) {
            ElMessage.warning('请先选择规格')
            return
        }
        if (displayStock.value <= 0) {
            ElMessage.warning('该商品暂时缺货')
            return
        }
        openBuyDialog()
    }

    const loadFurnitureDetail = async (id) => {
        if (!id) {
            ElMessage.error('家具ID不能为空')
            loading.value = false
            return
        }

        try {
            loading.value = true
            const res = await getFurnitureById(id)

            if (res.success && res.data) {
                furniture.value = res.data
            } else if (res.code === 200 && res.data) {
                furniture.value = res.data
            } else {
                furniture.value = {}
                ElMessage.warning(res.msg || '家具不存在')
            }
        } catch (error) {
            logger.error('加载家具详情失败:', error)
            furniture.value = {}
        } finally {
            loading.value = false
        }
    }

    const goBack = () => router.back()
    const goHome = () => router.push('/')

    return {
        furniture,
        loading,
        quantity,
        buyDialogVisible,
        buyLoading,
        buyForm,
        formatPrice,
        decreaseQty,
        increaseQty,
        addToCart,
        buyNow,
        openBuyDialog,
        closeBuyDialog,
        submitBuy,
        loadFurnitureDetail,
        goBack,
        goHome,
        // 规格相关
        specGroups,
        skuList,
        selectedSpecs,
        selectedSku,
        specLoading,
        hasSpecs,
        displayPrice,
        displayStock,
        displayImage,
        loadSpecs,
        selectSpec,
        isSpecValueAvailable
    }
}

// 家具列表页逻辑（分类详情）
export function useFurnitureList() {
    const router = useRouter()

    const furnitureList = ref([])
    const loading = ref(true)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const typeInfo = ref({})

    const loadFurnitureList = async (typeId) => {
        if (!typeId) {
            ElMessage.error('分类ID不能为空')
            loading.value = false
            return
        }

        try {
            loading.value = true
            const res = await getFurnitureByTypeId(typeId, currentPage.value, pageSize.value)

            if (res.success && res.data) {
                furnitureList.value = res.data.records || []
                total.value = res.data.total || 0

                if (furnitureList.value.length === 0 && currentPage.value > 1) {
                    currentPage.value = 1
                    loadFurnitureList(typeId)

                }
            } else if (res.code === 200 && res.data) {
                furnitureList.value = res.data.records || []
                total.value = res.data.total || 0

                if (furnitureList.value.length === 0 && currentPage.value > 1) {
                    currentPage.value = 1
                    loadFurnitureList(typeId)

                }
            } else {
                furnitureList.value = []
                total.value = 0
                ElMessage.warning(res.msg || '暂无家具数据')
            }
        } catch (error) {
            logger.error('加载家具列表失败:', error)
            furnitureList.value = []
            total.value = 0
        } finally {
            loading.value = false
        }
    }

    const handleSizeChange = (val, typeId) => {
        pageSize.value = val
        currentPage.value = 1
        loadFurnitureList(typeId)
    }

    const handleCurrentChange = (val, typeId) => {
        currentPage.value = val
        loadFurnitureList(typeId)
    }

    const goToDetail = (item) => {
        router.push({
            name: 'FurnitureDetail',
            params: {id: item.id}
        })
    }

    const goHome = () => router.push('/')

    const loadTypeInfo = (typeId) => {
        const cached = sessionStorage.getItem('currentType')
        if (cached) {
            const parsed = JSON.parse(cached)
            if (parsed.id == typeId) {
                typeInfo.value = parsed
            }
        }
        if (!typeInfo.value.name) {
            typeInfo.value.name = '家具系列'
        }
    }

    return {
        furnitureList,
        loading,
        currentPage,
        pageSize,
        total,
        typeInfo,
        formatPrice,
        loadFurnitureList,
        handleSizeChange,
        handleCurrentChange,
        goToDetail,
        goHome,
        loadTypeInfo
    }
}
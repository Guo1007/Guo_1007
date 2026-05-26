import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {getFurnitureById, getFurnitureByTypeId} from '@/api/furniture.js'
import {createOrder} from '@/api/order.js'
import {useCartStore} from '@/stores/cart.js'

const cartStore = useCartStore()

export function useFurnitureDetail() {
    const router = useRouter()

    const furniture = ref({})
    const loading = ref(true)
    const quantity = ref(1)

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

    const formatPrice = (price) => {
        if (!price) return '0.00'
        return parseFloat(price).toFixed(2)
    }

    const decreaseQty = () => {
        if (quantity.value > 1) quantity.value--
    }

    const increaseQty = () => {
        if (quantity.value < furniture.value.stock) quantity.value++
    }

    const addToCart = () => {
        cartStore.addItem(furniture.value, quantity.value)
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
        if (!buyForm.value.consignee.trim()) {
            ElMessage.warning('请输入收货人姓名')
            return false
        }
        if (!buyForm.value.phone.trim()) {
            ElMessage.warning('请输入联系电话')
            return false
        }
        if (!buyForm.value.address.trim()) {
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
                ElMessage.error(res.message || '订单创建失败')
                return false
            }
        } catch (error) {
            console.error('创建订单失败:', error)
            ElMessage.error(error.response?.data?.message || '订单创建失败，请稍后重试')
            return false
        } finally {
            buyLoading.value = false
        }
    }

    // 立即购买按钮点击
    const buyNow = () => {
        if (furniture.value.stock <= 0) {
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
                ElMessage.warning(res.message || '家具不存在')
            }
        } catch (error) {
            console.error('加载家具详情失败:', error)
            ElMessage.error('加载失败，请稍后重试')
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
        goHome
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

    const formatPrice = (price) => {
        if (!price) return '0.00'
        return parseFloat(price).toFixed(2)
    }

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
                ElMessage.warning(res.message || '暂无家具数据')
            }
        } catch (error) {
            console.error('加载家具列表失败:', error)
            ElMessage.error('加载失败，请稍后重试')
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
            params: { id: item.id }
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
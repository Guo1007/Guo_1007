<template>
    <div class="cart-wrapper">
        <!-- 购物车悬浮按钮 -->
        <div class="cart-float-btn" :style="btnStyle" @mousedown="startDrag" @touchstart="startDrag"
            @click="handleClick" v-show="!cartStore.isOpen">
            <el-badge :value="cartStore.totalCount" :hidden="cartStore.isEmpty" class="cart-badge">
                <div class="cart-icon">🛒</div>
            </el-badge>
            <div class="cart-price" v-if="!cartStore.isEmpty">¥{{ cartStore.totalPrice }}</div>
        </div>

        <!-- 购物车抽屉 -->
        <el-drawer v-model="cartStore.isOpen" title="购物车" size="400px" :with-header="true" class="cart-drawer">
            <div class="cart-content">
                <!-- 空状态 -->
                <div v-if="cartStore.isEmpty" class="cart-empty">
                    <div class="empty-icon">🛒</div>
                    <p>购物车是空的</p>
                    <el-button type="primary" @click="goShopping">去逛逛</el-button>
                </div>

                <!-- 商品列表 -->
                <div v-else class="cart-list">
                    <div v-for="item in cartStore.items" :key="item.id" class="cart-item">
                        <img :src="item.fIcon ? 'http://localhost:8080' + item.fIcon : '/images/default-furniture.png'"
                            class="item-img" @click="goToDetail(item.id)" />
                        <div class="item-info">
                            <h4 @click="goToDetail(item.id)">{{ item.fName }}</h4>
                            <p class="item-price">¥{{ formatPrice(item.price) }}</p>
                        </div>
                        <div class="item-actions">
                            <div class="quantity-control">
                                <button class="qty-btn" @click="cartStore.decreaseQuantity(item.id)">-</button>
                                <span class="qty-value">{{ item.quantity }}</span>
                                <button class="qty-btn" @click="cartStore.increaseQuantity(item.id)"
                                    :disabled="item.quantity >= item.stock">+</button>
                            </div>
                            <div class="item-total">¥{{ formatPrice(item.price * item.quantity) }}</div>
                            <el-button type="danger" text size="small" @click="cartStore.removeItem(item.id)"
                                class="delete-btn">
                                删除
                            </el-button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 底部结算栏 -->
            <template #footer v-if="!cartStore.isEmpty">
                <div class="cart-footer">
                    <div class="footer-info">
                        <span class="total-count">共 {{ cartStore.totalCount }} 件</span>
                        <span class="total-price">合计：¥{{ cartStore.totalPrice }}</span>
                    </div>
                    <div class="footer-actions">
                        <el-button type="danger" plain @click="cartStore.clearCart">清空</el-button>
                        <el-button type="primary" size="large" @click="checkout">去结算</el-button>
                    </div>
                </div>
            </template>
        </el-drawer>
    </div>
</template>

<script setup>
import { useCartStore } from '@/stores/cart.js'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createOrder } from '@/api/order.js' 
import { ref, computed, onMounted, onUnmounted } from 'vue'
import '@/styles/views/cart.scss'

const cartStore = useCartStore()
const router = useRouter()

const btnPosition = ref({ x: 0, y: 0 })
const isDragging = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const btnStart = ref({ x: 0, y: 0 })
const hasMoved = ref(false)

onMounted(() => {
    const saved = localStorage.getItem('cartBtnPosition')
    if (saved) {
        btnPosition.value = JSON.parse(saved)
    } else {
        btnPosition.value = {
            x: window.innerWidth - 100,
            y: window.innerHeight - 150
        }
    }
})

const btnStyle = computed(() => ({
    left: btnPosition.value.x + 'px',
    top: btnPosition.value.y + 'px',
    cursor: isDragging.value ? 'grabbing' : 'grab'
}))

// 开始拖动
const startDrag = (e) => {
    isDragging.value = true
    hasMoved.value = false
    const clientX = e.touches ? e.touches[0].clientX : e.clientX
    const clientY = e.touches ? e.touches[0].clientY : e.clientY
    dragStart.value = { x: clientX, y: clientY }
    btnStart.value = { ...btnPosition.value }
    document.addEventListener('mousemove', onDrag)
    document.addEventListener('mouseup', stopDrag)
    document.addEventListener('touchmove', onDrag)
    document.addEventListener('touchend', stopDrag)
}

// 拖动中
const onDrag = (e) => {
    if (!isDragging.value) return

    const clientX = e.touches ? e.touches[0].clientX : e.clientX
    const clientY = e.touches ? e.touches[0].clientY : e.clientY
    const dx = Math.abs(clientX - dragStart.value.x)
    const dy = Math.abs(clientY - dragStart.value.y)
    if (dx > 5 || dy > 5) {
        hasMoved.value = true
    }
    if (hasMoved.value) {
        e.preventDefault()
    }
    let newX = btnStart.value.x + (clientX - dragStart.value.x)
    let newY = btnStart.value.y + (clientY - dragStart.value.y)
    const maxX = window.innerWidth - 80
    const maxY = window.innerHeight - 80
    newX = Math.max(10, Math.min(newX, maxX))
    newY = Math.max(10, Math.min(newY, maxY))
    btnPosition.value = { x: newX, y: newY }
}

// 停止拖动
const stopDrag = () => {
    isDragging.value = false
    localStorage.setItem('cartBtnPosition', JSON.stringify(btnPosition.value))
    document.removeEventListener('mousemove', onDrag)
    document.removeEventListener('mouseup', stopDrag)
    document.removeEventListener('touchmove', onDrag)
    document.removeEventListener('touchend', stopDrag)
}

// 点击处理 - 只有没移动过才打开购物车
const handleClick = () => {
    if (hasMoved.value) {
        hasMoved.value = false
        return
    }
    cartStore.toggleCart()
}

const formatPrice = (price) => {
    if (!price) return '0.00'
    return parseFloat(price).toFixed(2)
}

const goShopping = () => {
    cartStore.closeCart()
    router.push('/')
}

const goToDetail = (id) => {
    cartStore.closeCart()
    router.push(`/furniture/detail/${id}`)
}

const checkout = async () => {
    if (cartStore.isEmpty) {
        ElMessage.warning('购物车是空的')
        return
    }

    const userInfoStr = localStorage.getItem('userInfo')
    let consignee = ''
    let phone = ''
    let address = ''

    if (userInfoStr) {
        try {
            const userInfo = JSON.parse(userInfoStr)
            consignee = userInfo.userName || ''
            phone = userInfo.phone || ''
            address = userInfo.address || ''
        } catch (e) { }
    }
    if (!consignee || !phone || !address) {
        ElMessage.warning('请先完善收货地址信息')
        cartStore.closeCart()
        router.push('/user/profile')
        return
    }
    try {
        const orderData = {
            consignee,
            phone,
            address,
            remark: '',
            itemList: cartStore.getCartData()
        }

        const res = await createOrder(orderData)
        if (res.success || res.code === 200) {
            cartStore.checkout()
            cartStore.closeCart()
            ElMessage.success('订单创建成功')
            router.push('/user/orders')
        } else {
            ElMessage.error(res.message || '订单创建失败')
        }
    } catch (error) {
        console.error('创建订单失败:', error)
        ElMessage.error('订单创建失败')
    }
}
</script>
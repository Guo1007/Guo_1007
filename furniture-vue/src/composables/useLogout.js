import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userLogout } from '@/api/user'
import { useCartStore } from '@/stores/cart'

export function useLogout() {
    const router = useRouter()
    const cartStore = useCartStore()

    const logout = () => {
        ElMessageBox.confirm('确定要退出登录吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        }).then(async () => {
            try {
                await userLogout()
            } catch (error) {
                console.error('退出登录服务器同步失败', error)
            } finally {
                localStorage.removeItem('token')
                localStorage.removeItem('userInfo')
                localStorage.removeItem('userName')
                localStorage.removeItem('userIcon')
                localStorage.removeItem('userEmail')
                sessionStorage.clear()
                cartStore.clearState()
                ElMessage.success('已安全退出')
                router.push('/login')
            }
        }).catch(() => {})
    }

    return { logout }
}

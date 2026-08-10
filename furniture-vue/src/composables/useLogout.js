import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { userLogout } from "@/api/user";
import { logger } from "@/utils/logger.js";
import { useCartStore } from "@/stores/cart";
import { useUserStore } from "@/stores/user";

export function useLogout() {
  const router = useRouter();
  const cartStore = useCartStore();
  const userStore = useUserStore();

  const logout = () => {
    ElMessageBox.confirm("确定要退出登录吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    })
      .then(async () => {
        try {
          await userLogout();
        } catch (error) {
          logger.error("退出登录服务器同步失败", error);
        } finally {
          // 走 userStore.logout() 同步响应式登录态，使导航栏立即切换为未登录状态
          userStore.logout();
          localStorage.removeItem("userName");
          localStorage.removeItem("userIcon");
          localStorage.removeItem("userEmail");
          sessionStorage.clear();
          cartStore.clearState();
          ElMessage.success("已安全退出");
          router.push("/");
        }
      })
      .catch(() => {});
  };

  return { logout };
}

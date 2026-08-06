import { useRouter } from "vue-router";
import { ElMessageBox } from "element-plus";

/**
 * 登录引导 composable。
 * <p>
 * 游客执行需要登录的操作（收藏、下单、评价、结算等）时，
 * 统一弹出确认框引导前往登录页，登录成功后自动跳回原页面。
 * </p>
 */
export function useRequireLogin() {
  const router = useRouter();

  /**
   * 检查是否已登录；未登录时弹出引导并跳转登录页。
   *
   * @param {string} msg 引导提示文案，默认"该操作需要登录"
   * @returns {boolean} 已登录返回 true，未登录返回 false
   */
  const requireLogin = (msg = "该操作需要登录") => {
    if (localStorage.getItem("token")) return true;
    ElMessageBox.confirm(`${msg}，是否前往登录？`, "提示", {
      confirmButtonText: "去登录",
      cancelButtonText: "取消",
      type: "warning",
    })
      .then(() => {
        router.push({
          path: "/login",
          query: { redirect: router.currentRoute.value.fullPath },
        });
      })
      .catch(() => {});
    return false;
  };

  return { requireLogin };
}

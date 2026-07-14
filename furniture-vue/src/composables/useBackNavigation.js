import { useRouter } from 'vue-router'

export function useBackNavigation() {
  const router = useRouter()

  const goBack = (fallback = '/') => {
    // history.length > 2 表示存在上一页记录（页面内至少有1条前进历史）
    if (window.history.length > 2) {
      router.back()
    } else {
      router.push(fallback)
    }
  }

  return { goBack }
}

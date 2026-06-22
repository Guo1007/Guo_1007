import {defineStore} from 'pinia'
import {computed, ref} from 'vue'

export const useUserStore = defineStore('user', () => {
    // State
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

    // Getters
    const isLoggedIn = computed(() => !!token.value)

    // Actions
    const setToken = (newToken) => {
        token.value = newToken
        localStorage.setItem('token', newToken)
    }

    const setUserInfo = (info) => {
        userInfo.value = info
        localStorage.setItem('userInfo', JSON.stringify(info))
    }

    const logout = () => {
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
    }

    return {
        token,
        userInfo,
        isLoggedIn,
        setToken,
        setUserInfo,
        logout
    }
})
import axios from 'axios'
import {ElMessage} from 'element-plus'
import router from '@/router'

const service = axios.create({
    baseURL: '/api',
    timeout: 5000
})

const WHITE_LIST = [
    '/user/login',
    '/user/register',
    '/user/code', 
    '/user/r_code',    
    '/auth/login',   
    '/auth/register'
]

service.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

service.interceptors.response.use(
    response => {
        if (response.config.responseType === 'blob' || response.data instanceof Blob) {
            return response.data
        }
        const res = response.data
        const isSuccess = res.code === 200 || res.code === '200' || res.success === true

        if (!isSuccess) {
            if (res.code === 401 || res.code === '401') {
                localStorage.removeItem('token')
                if (router.currentRoute.value.path !== '/login') {
                    ElMessage.error(res.msg || '登录已过期，请重新登录')
                    router.push('/login')
                }
                return Promise.reject(new Error(res.msg || '登录已过期'))
            }
            const isWhite = WHITE_LIST.some(path => response.config.url.includes(path))
            if (!isWhite) {
                ElMessage.error(res.msg || res.errorMsg || '操作失败')
            }

            return Promise.reject(new Error(res.msg || res.errorMsg || '操作失败'))
        }
        return res
    },
    error => {
        if (error.response) {
            const status = error.response.status
            let message = '系统错误'

            if (status === 401) {
                message = '未授权，请重新登录'
                localStorage.removeItem('token')
                if (router.currentRoute.value.path !== '/login') {
                    router.push('/login')
                }
            } else if (status === 404) {
                message = '请求地址不存在'
            } else if (status === 500) {
                message = '服务器内部错误'
            } else {
                message = error.response.data?.msg || `连接错误：${status}`
            }
            const isWhite = WHITE_LIST.some(path => error.config?.url?.includes(path))
            if (!isWhite || status === 401) {
                ElMessage.error(message)
            }

            return Promise.reject(error)
        }
        let message = error.message
        if (message.includes('timeout')) message = '请求超时'
        else if (message.includes('Network')) message = '网络连接失败'
        const isWhite = WHITE_LIST.some(path => error.config?.url?.includes(path))
        if (!isWhite) {
            ElMessage.error(message)
        }
        return Promise.reject(error)
    }
)

export default service
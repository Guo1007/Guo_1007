import request from './request'

/**
 * 发送验证码
 * @param {Object} data - { phone: '13800138000' }
 */
export const sendCode = (data) => {
    return request({
        url: '/user/code',
        method: 'post',
        data
    })
}

export const sendRegisterCode = (data) => {
    return request({
        url: '/user/r_code',
        method: 'post',
        data
    })
}

/**
 * 注册
 *
 *
 */
export function register(data) {
    return request({
        url: '/user/register', // 确保后端有这个接口
        method: 'post',
        data
    })
}

/**
 * 登录
 *
 */
export const login = (data) => {
    return request({
        url: '/user/login',
        method: 'post',
        data
    })
}


export function userLogout() {
    return request({
        url: '/user/logout',
        method: 'post'
    })
}

/**
 * 获取当前登录用户信息（需要token）
 *
 */
export const getUserInfo = () => {
    return request({
        url: '/user/me',
        method: 'get'
    })
}

export function updateUserProfile(data) {
    return request({
        url: '/user/update',
        method: 'put',
        data
    })
}

export function updatePassword(data) {
    return request({
        url: '/user/password',
        method: 'put',
        data
    })
}

export const uploadAvatar = (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/user/upload/avatar',
        method: 'post',
        data: formData,
        headers: {'Content-Type': 'multipart/form-data'}
    })
}
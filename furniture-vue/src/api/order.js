import request from './request'

export const createOrder = (data) => {
    return request({
        url: '/order/create',
        method: 'post',
        data
    })
}

export const getUserOrders = (params) => {
    return request({
        url: '/order/list',
        method: 'get',
        params
    })
}

export const getOrderDetail = (orderId) => {
    return request({
        url: `/order/detail/${orderId}`,
        method: 'get'
    })
}

export const cancelOrder = (orderId) => {
    return request({
        url: `/order/cancel/${orderId}`,
        method: 'put'
    })
}

export const payOrder = (orderId) => {
    return request({
        url: `/order/pay/${orderId}`,
        method: 'put'
    })
}

export function confirmReceipt(orderId) {
    return request({
        url: `/order/confirm/${orderId}`,
        method: 'put'
    })
}
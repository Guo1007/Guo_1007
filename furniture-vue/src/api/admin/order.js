import request from '@/api/request'

export const getOrderList = (params) => {
    return request({
        url: '/admin/order/list',
        method: 'get',
        params
    })
}

export function shipOrder(orderId) {
    return request({
        url: `/admin/order/ship/${orderId}`,
        method: 'put'
    })
}

export function exportOrders() {
    return request({
        url: '/admin/order/export',
        method: 'get',
        responseType: 'blob'
    })
}
import service from '../request'

export function getDashboardStats() {
    return service.get('/admin/dashboard/stats')
}

export function getOrderTrend() {
    return service.get('/admin/dashboard/order-trend')
}

export function getTopFurniture() {
    return service.get('/admin/dashboard/top-furniture')
}

export function getLowStock() {
    return service.get('/admin/dashboard/low-stock')
}

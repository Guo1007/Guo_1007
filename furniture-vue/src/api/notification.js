import service from './request'

export function getNotificationList(current = 1, size = 10) {
    return service.get('/notification/list', {params: {current, size}})
}

export function getUnreadCount() {
    return service.get('/notification/unread-count')
}

export function markAsRead(id) {
    return service.put(`/notification/read/${id}`)
}

export function markAllAsRead() {
    return service.put('/notification/read-all')
}

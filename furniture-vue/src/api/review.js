import request from './request'

export const getReviews = (furnitureId) => request.get(`/review/list/${furnitureId}`)

export const addReview = (data) => request.post('/review/add', data)

export const getOrderReviews = (orderId) => request.get(`/review/order/${orderId}`)

export const deleteReview = (reviewId) => request.delete(`/review/${reviewId}`)

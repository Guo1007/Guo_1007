import request from './request'

export const getReviewComments = (reviewId) =>
    request.get(`/review-comment/list/${reviewId}`)

export const addReviewComment = (data) =>
    request.post('/review-comment/add', data)

export const deleteReviewComment = (commentId) =>
    request.delete(`/review-comment/${commentId}`)

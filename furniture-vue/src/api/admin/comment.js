import request from '@/api/request'

// ========== 商品评价 ==========

export const getPendingComments = (params) =>
    request({url: '/admin/comment/list', method: 'get', params})

export const approveComment = (id) =>
    request({url: `/admin/comment/approve/${id}`, method: 'put'})

export const rejectComment = (id) =>
    request({url: `/admin/comment/reject/${id}`, method: 'put'})

// ========== 追评 ==========

export const getPendingAppends = (params) =>
    request({url: '/admin/comment/append/list', method: 'get', params})

export const approveAppend = (id) =>
    request({url: `/admin/comment/append/approve/${id}`, method: 'put'})

export const rejectAppend = (id) =>
    request({url: `/admin/comment/append/reject/${id}`, method: 'put'})

// ========== 评价评论 ==========

export const getPendingReviewComments = (params) =>
    request({url: '/admin/comment/review-comment/list', method: 'get', params})

export const approveReviewComment = (id) =>
    request({url: `/admin/comment/review-comment/approve/${id}`, method: 'put'})

export const rejectReviewComment = (id) =>
    request({url: `/admin/comment/review-comment/reject/${id}`, method: 'put'})

// ========== 删除 ==========

export const deleteComment = (id) =>
    request({url: `/admin/comment/${id}`, method: 'delete'})

export const batchDeleteComments = (ids) =>
    request({url: '/admin/comment/batch', method: 'delete', data: ids})

export const deleteAppend = (id) =>
    request({url: `/admin/comment/append/${id}`, method: 'delete'})

export const batchDeleteAppends = (ids) =>
    request({url: '/admin/comment/append/batch', method: 'delete', data: ids})

export const deleteReviewComment = (id) =>
    request({url: `/admin/comment/review-comment/${id}`, method: 'delete'})

export const batchDeleteReviewComments = (ids) =>
    request({url: '/admin/comment/review-comment/batch', method: 'delete', data: ids})

export const getPendingCommentCount = () =>
    request({url: '/admin/comment/pending-count', method: 'get'})

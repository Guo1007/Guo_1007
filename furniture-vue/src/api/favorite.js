import request from './request'

export const getFavorites = () => request.get('/favorite/list')

export const checkFavorite = (furnitureId) => request.get(`/favorite/check/${furnitureId}`)

export const toggleFavorite = (furnitureId) => request.post(`/favorite/toggle/${furnitureId}`)

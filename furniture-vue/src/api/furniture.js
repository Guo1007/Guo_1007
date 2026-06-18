// furniture.js
import request from './request'

export const getFurnitureTypeList = () => {
    return request({
        url: '/furniture_type/list',
        method: 'get'
    })
}

export function getFurnitureByTypeId(params) {
    if (typeof params === 'number' || typeof params === 'string') {
        const typeId = params
        const current = arguments[1] || 1
        const size = arguments[2] || 10
        return request({
            url: '/furniture/list',
            method: 'get',
            params: { typeId, current, size }
        })
    }

    return request({
        url: '/furniture/list',
        method: 'get',
        params
    })
}

export const getFurnitureById = (id) => {
    return request({
        url: `/furniture/${id}`,
        method: 'get'
    })
}

export const getFurnitureBrands = (typeId) => {
    return request({
        url: '/furniture/brands',
        method: 'get',
        params: { typeId }
    })
}

/** 查询商品的规格+SKU（客户端展示用） */
export const getFurnitureSpecs = (id) => {
    return request({
        url: `/furniture/${id}/specs`,
        method: 'get'
    })
}
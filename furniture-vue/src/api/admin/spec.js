import request from '@/api/request'

/** 查询商品的规格+SKU（管理端编辑用） */
export const getSpecAndSku = (furnitureId) => {
    return request({
        url: `/admin/spec/${furnitureId}`,
        method: 'get'
    })
}

/** 保存商品的规格与SKU（全量替换） */
export const saveSpecAndSku = (data) => {
    return request({
        url: '/admin/spec/save',
        method: 'post',
        data
    })
}
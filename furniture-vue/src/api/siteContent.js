import request from './request'

/** 获取所有启用的站点内容（按分组归类） */
export const getSiteContent = () => {
    return request({ url: '/api/site-content', method: 'get' })
}

/** 管理员：获取全部列表 */
export const getAdminSiteContentList = () => {
    return request({ url: '/admin/site-content', method: 'get' })
}

/** 管理员：保存 */
export const saveSiteContent = (data) => {
    return request({ url: '/admin/site-content', method: 'post', data })
}

/** 管理员：切换启用 */
export const toggleSiteContent = (id) => {
    return request({ url: `/admin/site-content/${id}/toggle`, method: 'put' })
}

/** 管理员：删除 */
export const deleteSiteContent = (id) => {
    return request({ url: `/admin/site-content/${id}`, method: 'delete' })
}

/** 管理员：上传图片 */
export const uploadSiteContentImage = (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request({ url: '/admin/site-content/upload', method: 'post', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
}

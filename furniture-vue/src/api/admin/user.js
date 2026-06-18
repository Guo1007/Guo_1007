import request from '@/api/request'

export const getUserList = (params) => {
    return request({
        url: '/admin/user/list',
        method: 'get',
        params
    })
}

export function editUser(data) {
    return request({
        url: '/admin/user/edit',
        method: 'put',
        data
    })
}

export function deleteUser(id) {
    return request({
        url: `/admin/user/delete/${id}`,
        method: 'delete'
    })
}

export function getSimpleUserList(keyword = '') {
    return request({
        url: '/admin/user/simple',
        method: 'get',
        params: {keyword}
    })
}
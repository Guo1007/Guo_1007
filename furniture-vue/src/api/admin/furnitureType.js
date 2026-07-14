import request from "@/api/request"; // 引入你封装好的 request.js

// 获取列表
export function getFurnitureTypeList(params) {
  return request({
    url: "/admin/furniture_type/list",
    method: "get",
    params,
  });
}

// 获取详情
export function getFurnitureTypeInfo(id) {
  return request({
    url: `/admin/furniture_type/info/${id}`,
    method: "get",
  });
}

// 新增
export function addFurnitureType(data) {
  return request({
    url: "/admin/furniture_type/add",
    method: "post",
    data,
  });
}

// 修改
export function updateFurnitureType(data) {
  return request({
    url: "/admin/furniture_type/update",
    method: "put",
    data,
  });
}

export function uploadTypeIcon(file) {
  const formData = new FormData();
  formData.append("file", file);
  return request({
    url: "/admin/furniture_type/upload",
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

// 删除
export function deleteFurnitureType(id) {
  return request({
    url: `/admin/furniture_type/delete/${id}`,
    method: "delete",
  });
}

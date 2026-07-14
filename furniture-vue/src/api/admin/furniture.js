import request from "@/api/request";

// 获取列表
export const getFurnitureList = (params) => {
  return request({
    url: "/admin/furniture/list",
    method: "get",
    params,
  });
};

// 新增家具
export const addFurniture = (data) => {
  return request({
    url: "/admin/furniture/add",
    method: "post",
    data,
  });
};

// 编辑家具
export const editFurniture = (data) => {
  return request({
    url: "/admin/furniture/edit",
    method: "put",
    data,
  });
};

export function uploadFurnitureImage(file) {
  const formData = new FormData();
  formData.append("file", file);
  return request({
    url: "/admin/furniture/upload",
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

// 删除家具
export const deleteFurniture = (id) => {
  return request({
    url: `/admin/furniture/delete/${id}`,
    method: "delete",
  });
};

import request from "./request";

export const getAddressList = () => request.get("/address/list");

export const saveAddress = (data) => request.post("/address/save", data);

export const deleteAddress = (id) => request.delete(`/address/delete/${id}`);

export const setDefaultAddress = (id) => request.put(`/address/default/${id}`);

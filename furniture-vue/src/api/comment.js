import request from "./request";

export const getComments = (goodsId, current = 1, size = 10) =>
  request.get(`/comment/list/${goodsId}`, { params: { current, size } });

export const getCommentsByOrderId = (orderId) =>
  request.get(`/comment/list/order/${orderId}`);

export const addComment = (data) => request.post("/comment/add", data);

export const appendComment = (data) => request.post("/comment/append", data);

export const deleteComment = (commentId) =>
  request.delete(`/comment/${commentId}`);

export const deleteAppend = (appendId) =>
  request.delete(`/comment/append/${appendId}`);

export const deleteReview = (reviewId) =>
  request.delete(`/comment/review/${reviewId}`);

export const uploadCommentImage = (file) => {
  const formData = new FormData();
  formData.append("file", file);
  return request.post("/comment/upload/image", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export const uploadCommentVideo = (file) => {
  const formData = new FormData();
  formData.append("file", file);
  return request.post("/comment/upload/video", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

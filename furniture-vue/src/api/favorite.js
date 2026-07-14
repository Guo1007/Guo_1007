import request from "./request";

export const getFavorites = (current = 1, size = 20) =>
  request.get("/favorite/list", { params: { current, size } });

export const checkFavorite = (furnitureId) =>
  request.get(`/favorite/check/${furnitureId}`);

export const toggleFavorite = (furnitureId) =>
  request.post(`/favorite/toggle/${furnitureId}`);

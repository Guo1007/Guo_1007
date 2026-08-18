import request from "@/api/request";

/**
 * 获取待审核的用户资料列表
 */
export function getPendingProfileReviews(page, size, type) {
  return request.get("/admin/profile-review/list", {
    params: { page, size, type },
  });
}

/**
 * 审核通过昵称
 */
export function approveNickname(userId) {
  return request.put(`/admin/profile-review/nickname/approve/${userId}`);
}

/**
 * 拒绝昵称
 */
export function rejectNickname(userId, reason) {
  return request.put(`/admin/profile-review/nickname/reject/${userId}`, { reason });
}

/**
 * 审核通过头像
 */
export function approveIcon(userId) {
  return request.put(`/admin/profile-review/icon/approve/${userId}`);
}

/**
 * 拒绝头像
 */
export function rejectIcon(userId, reason) {
  return request.put(`/admin/profile-review/icon/reject/${userId}`, { reason });
}
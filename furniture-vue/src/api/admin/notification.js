import service from "../request";

export function sendNotification(data) {
  return service.post("/admin/notification/send", data);
}

export function getAdminNotificationList(current = 1, size = 10, type = "") {
  return service.get("/admin/notification/list", {
    params: { current, size, type },
  });
}

export function updateNotification(id, data) {
  return service.put(`/admin/notification/update/${id}`, data);
}

export function deleteNotification(id) {
  return service.delete(`/admin/notification/delete/${id}`);
}

export function batchDeleteNotifications(ids) {
  return service.delete("/admin/notification/batch", { data: ids });
}

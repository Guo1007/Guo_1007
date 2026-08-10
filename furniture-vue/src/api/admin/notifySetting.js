import request from "@/api/request";

export function getNotifySetting() {
  return request({
    url: "/admin/notify-setting",
    method: "get",
  });
}

export function saveNotifySetting(data) {
  return request({
    url: "/admin/notify-setting",
    method: "put",
    data,
  });
}

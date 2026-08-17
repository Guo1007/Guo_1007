import request from "@/api/request";

export const getRejectReasons = () =>
  request({ url: "/admin/reject-reason/list", method: "get" });
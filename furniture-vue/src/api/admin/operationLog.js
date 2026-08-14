import service from "../request";

export function getOperationLogs(params) {
  return service.get("/admin/operation-logs", { params });
}
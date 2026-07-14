import axios from "axios";
import { ElMessage } from "element-plus";
import router from "@/router";

const service = axios.create({
  baseURL: "/api",
  timeout: 5000,
});

const WHITE_LIST = [
  "/user/login",
  "/user/register",
  "/user/code",
  "/user/r_code",
  "/auth/login",
  "/auth/register",
  "/ai/chat",
];

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers["Authorization"] = "Bearer " + token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

service.interceptors.response.use(
  (response) => {
    if (
      response.config.responseType === "blob" ||
      response.data instanceof Blob
    ) {
      return response.data;
    }
    const res = response.data;
    const isSuccess =
      res.code === 200 || res.code === "200" || res.success === true;

    if (!isSuccess) {
      // 401：拦截器统一处理跳转登录页
      if (res.code === 401 || res.code === "401") {
        localStorage.removeItem("token");
        if (router.currentRoute.value.path !== "/login") {
          ElMessage.error(res.msg || "登录已过期，请重新登录");
          router.push("/login");
        }
        return Promise.reject(res);
      }
      // 其他业务错误：不弹 toast，不抛异常，正常返回 res
      // 由组件 else 分支用 res.msg 展示，一个地方弹一次
      return res;
    }
    return res;
  },
  (error) => {
    // 网络/HTTP 层错误：拦截器统一弹 toast（只弹一次）
    // 组件 catch 块不要再弹 toast
    let message = "系统错误";
    if (error.response) {
      const status = error.response.status;
      if (status === 401) {
        message = "未授权，请重新登录";
        localStorage.removeItem("token");
        if (router.currentRoute.value.path !== "/login") {
          router.push("/login");
        }
      } else if (status === 404) {
        message = "请求地址不存在";
      } else if (status === 500) {
        message = "服务器内部错误";
      } else {
        message = error.response.data?.msg || `连接错误：${status}`;
      }
    } else {
      if (error.message?.includes("timeout")) {
        message = "请求超时，请稍后重试";
      } else if (error.message?.includes("Network")) {
        message = "网络连接失败，请检查网络";
      } else {
        message = "网络异常，请检查网络连接";
      }
    }
    ElMessage.error(message);
    return Promise.reject(error);
  },
);

export default service;

package com.example.furnituresystem.config;

import cn.hutool.json.JSONUtil;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminIntercept implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (UserHolder.getUser() == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(JSONUtil.toJsonStr(Result.fail(401, "请先登录")));
            return false;
        }
        if (UserHolder.getUser().getIsAdmin() != 1) {
            response.setStatus(403);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(JSONUtil.toJsonStr(Result.fail(403, "权限不足")));
            return false;
        }
        return true;
    }
}

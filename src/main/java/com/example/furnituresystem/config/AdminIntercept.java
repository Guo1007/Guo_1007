package com.example.furnituresystem.config;

import com.example.furnituresystem.entity.dto.UserDTO;
import com.example.furnituresystem.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminIntercept implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserDTO userDTO = UserHolder.getUser();
        if (userDTO == null) {
            response.setStatus(401);
            return false;
        }
        if (!(userDTO.getIsAdmin() == 1)) {
            response.setStatus(403);
            return false;
        }
        return true;
    }
}

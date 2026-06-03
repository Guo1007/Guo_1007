package com.example.furnituresystem.security;

import cn.hutool.core.bean.BeanUtil;
import com.example.furnituresystem.entity.dto.UserDTO;
import com.example.furnituresystem.utils.RedisConstants;
import com.example.furnituresystem.utils.UserHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Token 认证过滤器
 * <p>
 * 从请求头中提取 Token，查询 Redis 获取用户信息，
 * 构建 {@link TokenAuthentication} 并设置到 SecurityContext。
 * 同时兼容原有 {@link UserHolder}，保证 Service 层代码无需改动。
 */
@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    private final StringRedisTemplate stringRedisTemplate;

    public TokenAuthFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String header = request.getHeader("authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = header.substring(7);
            String key = RedisConstants.LOGIN_USER_KEY + token;
            Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);

            if (userMap.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            // 刷新 Token 过期时间
            stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL,
                    java.util.concurrent.TimeUnit.SECONDS);

            UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
            TokenAuthentication auth = new TokenAuthentication(userDTO, token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            // 兼容原有 UserHolder
            UserHolder.saveUser(userDTO, token);

        } catch (Exception e) {
            logger.error("Token 认证异常", e);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后清理 UserHolder（与原 RefreshTokenIntercept 行为一致）
            UserHolder.removeUser();
        }
    }
}

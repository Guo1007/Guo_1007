package gcy.system.security;

import cn.hutool.core.bean.BeanUtil;
import gcy.system.entity.dto.UserDTO;
import gcy.system.utils.RedisConstants;
import gcy.system.utils.UserHolder;
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
import java.util.concurrent.TimeUnit;

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

            stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL,
                    TimeUnit.SECONDS);

            UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
            TokenAuthentication auth = new TokenAuthentication(userDTO, token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            UserHolder.saveUser(userDTO, token);

        } catch (Exception e) {
            logger.error("Token 认证异常", e);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserHolder.removeUser();
        }
    }
}

package com.example.furnituresystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC 配置
 * <p>
 * 原有的 LoginIntercept / AdminIntercept / RefreshTokenIntercept
 * 已由 Spring Security（{@link com.example.furnituresystem.security.SecurityConfig}）统一接管。
 * 此处仅保留静态资源配置。
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:D:/furniture-images/");
    }

}

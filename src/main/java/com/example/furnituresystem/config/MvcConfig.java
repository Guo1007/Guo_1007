package com.example.furnituresystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    private final LoginIntercept loginIntercept;

    private final AdminIntercept adminIntercept;

    private final RefreshTokenIntercept refreshTokenIntercept;

    public MvcConfig(LoginIntercept loginIntercept, AdminIntercept adminIntercept, RefreshTokenIntercept refreshTokenIntercept) {
        this.loginIntercept = loginIntercept;
        this.adminIntercept = adminIntercept;
        this.refreshTokenIntercept = refreshTokenIntercept;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(adminIntercept)
                .addPathPatterns("/admin/**")
                .order(1);

        registry.addInterceptor(loginIntercept).excludePathPatterns(
                "/shop/**",
                "/voucher/**",
                "/uploads/**",
                "/shop-type/**",
                "/blog/hot",
                "/user/code",
                "/user/r_code",
                "/user/login",
                "/user/register",
                "/images/**",
                "/monitor/**"
        ).order(1);

        registry.addInterceptor(refreshTokenIntercept).order(0);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:D:/furniture-images/");
    }

}

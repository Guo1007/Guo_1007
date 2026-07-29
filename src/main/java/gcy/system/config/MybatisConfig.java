package gcy.system.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类。
 * <p>
 * 注册 MyBatis-Plus 核心拦截器，并添加 MySQL 分页内部拦截器，
 * 为数据访问层提供自动分页支持。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Configuration
public class MybatisConfig {

    /**
     * 创建 MyBatis-Plus 拦截器 Bean。
     * <p>
     * 配置分页内部拦截器，指定数据库类型为 MySQL，
     * 使 MyBatis-Plus 能够自动拦截查询并完成分页处理。
     * </p>
     *
     * @return MybatisPlusInterceptor 实例，已注入 MySQL 分页拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

}

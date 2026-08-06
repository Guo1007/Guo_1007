package gcy.system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Knife4j 接口文档自定义配置类。
 * <p>
 * 通过显式声明 {@link OpenAPI} Bean 设置接口文档的标题、版本、描述等信息。
 * description 内容来自 classpath:doc/home.md（Markdown 格式），
 * Knife4j 首页由前端 marked 库将其渲染为富文本介绍文档。
 * </p>
 *
 * @author 郭名城
 * @date 2026-08-06
 */
@Slf4j
@Configuration
public class Knife4jConfig {

    /**
     * 首页介绍文档（Markdown 格式，位于 classpath:doc/home.md）
     */
    @Value("classpath:doc/home.md")
    private Resource homeDoc;

    /**
     * 自定义 OpenAPI 文档元信息。
     * <p>
     * 显式声明 OpenAPI Bean 是 springdoc 官方推荐的自定义方式，
     * 生成的 /v3/api-docs 会使用这里的标题、版本、描述与联系方式，
     * Knife4j 首页据此渲染。
     * </p>
     *
     * @return 携带自定义元信息的 OpenAPI 实例
     */
    @Bean
    public OpenAPI furnitureOpenAPI() {
        log.info("初始化 OpenAPI 文档元信息：标题=家具商城 API 接口文档，版本=v1.1.0");
        return new OpenAPI()
                .info(new Info()
                        .title("家具商城 API 接口文档")
                        .description(readHomeDoc())
                        .version("v1.1.0")
                        .contact(new Contact()
                                .name("郭名城")
                                .email("guochengyang@example.com")));
    }

    /**
     * 读取首页 Markdown 文档内容。
     * <p>
     * 读取失败时返回降级文案，不影响接口文档的正常生成。
     * </p>
     *
     * @return Markdown 文档字符串，读取失败时返回默认说明
     */
    private String readHomeDoc() {
        try {
            String content = new String(homeDoc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            log.info("读取 Knife4j 首页 Markdown 文档成功，长度={}", content.length());
            return content;
        } catch (IOException e) {
            log.error("读取 Knife4j 首页 Markdown 文档失败", e);
            return "## 家具商城 API 接口文档\n\n接口文档加载失败，请检查 classpath:doc/home.md 文件是否存在。";
        }
    }
}

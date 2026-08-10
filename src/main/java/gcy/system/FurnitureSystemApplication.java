package gcy.system;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/**
 * 家具管理系统Spring Boot主启动类。
 * <p>
 * 该类是整个应用程序的入口点，通过{@link SpringApplication}启动Spring Boot应用。
 * 启用了异步任务支持（{@link EnableAsync}）、定时任务支持（{@link EnableScheduling}）
 * 以及配置属性扫描（{@link ConfigurationPropertiesScan}）。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@SpringBootApplication(scanBasePackages = {"gcy.system", "gcy.ai"})
@ConfigurationPropertiesScan
@EnableAsync
@EnableScheduling
public class FurnitureSystemApplication {

    /**
     * 应用初始化时统一 JVM 默认时区为东八区（北京时间），
     * 与数据库（Asia/Shanghai）及前端显示保持一致，避免时间相差 8 小时。
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    /**
     * 应用程序的主入口方法。
     * <p>
     * 通过{@link SpringApplication#run(Class, String...)}方法启动Spring Boot应用程序。
     * </p>
     *
     * @param args 命令行参数，可在启动时传入外部配置参数
     * @return 无返回值，该方法启动Spring容器后程序进入运行状态
     */
    public static void main(String[] args) {
        SpringApplication.run(FurnitureSystemApplication.class, args);
    }

}

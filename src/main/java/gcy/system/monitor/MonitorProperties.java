package gcy.system.monitor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 监控告警配置（对应 application.yml 中 monitor 段）
 */
@Data
@ConfigurationProperties(prefix = "monitor")
public class MonitorProperties {

    /**
     * 是否启用邮件告警
     */
    private boolean alertEnabled = false;

    /** 告警接收邮箱列表 */
    private List<String> alertEmails = List.of();
}

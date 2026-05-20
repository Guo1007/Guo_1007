package com.example.furnituresystem.monitor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "monitor")
public class MonitorProperties {

    /**
     * 是否启用邮件告警
     */
    private boolean alertEnabled = false;

    /**
     * 告警接收邮箱列表
     */
    private List<String> alertEmails = List.of();
}

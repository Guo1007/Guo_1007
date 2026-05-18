package com.example.furnituresystem.monitor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "monitor.alert")
public class AlertProperties {

    /** 是否启用告警 */
    private boolean enabled = false;

    /** 钉钉机器人 Webhook URL（非空时启用钉钉通知） */
    private String dingTalkWebhook = "";

    /** 告警静默期（秒），同类型错误告警后在此时间内不再重复告警 */
    private long silenceSeconds = 300;

    /** 时间窗口（秒），在此窗口内统计错误次数 */
    private long windowSeconds = 60;

    /** 时间窗口内触发告警的错误阈值 */
    private int threshold = 10;
}

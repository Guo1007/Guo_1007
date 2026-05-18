package com.example.furnituresystem.monitor;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertProperties alertProperties;

    /** 上次告警时间（按错误类型） */
    private final ConcurrentHashMap<String, LocalDateTime> lastAlertTime = new ConcurrentHashMap<>();

    /** 时间窗口内错误计数（按错误类型） */
    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public void onError(ErrorLogCollector.ErrorRecord record) {
        if (!alertProperties.isEnabled()) {
            return;
        }

        String type = record.getType();
        WindowCounter counter = counters.computeIfAbsent(type, k -> new WindowCounter());
        counter.increment();

        // 检查是否需要告警
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastAlert = lastAlertTime.get(type);
        if (lastAlert != null && lastAlert.plusSeconds(alertProperties.getSilenceSeconds()).isAfter(now)) {
            return; // 静默期内，不重复告警
        }

        long count = counter.getCount(now, alertProperties.getWindowSeconds());
        if (count >= alertProperties.getThreshold()) {
            sendAlert(type, count, record);
            lastAlertTime.put(type, now);
        }
    }

    private void sendAlert(String type, long count, ErrorLogCollector.ErrorRecord record) {
        String msg = String.format(
                "[告警] 错误类型: %s\n次数: %d (最近 %d 秒内)\n最近消息: %s\n请求路径: %s\n时间: %s",
                type, count, alertProperties.getWindowSeconds(),
                record.getMessage(), record.getRequestUri(), record.getFormattedTime()
        );
        log.warn("\n==================== 系统告警 ====================\n{}\n===========================================", msg);

        // 钉钉通知
        String webhook = alertProperties.getDingTalkWebhook();
        if (webhook != null && !webhook.isBlank()) {
            sendDingTalk(webhook, msg);
        }
    }

    private void sendDingTalk(String webhook, String text) {
        try {
            Map<String, Object> content = new HashMap<>();
            content.put("msgtype", "text");
            Map<String, String> textContent = new HashMap<>();
            textContent.put("content", text);
            content.put("text", textContent);
            HttpUtil.post(webhook, JSONUtil.toJsonStr(content), 5000);
            log.info("钉钉告警发送成功");
        } catch (Exception e) {
            log.error("钉钉告警发送失败", e);
        }
    }

    private static class WindowCounter {
        private long count;
        private LocalDateTime windowStart;

        WindowCounter() {
            this.count = 0;
            this.windowStart = LocalDateTime.now();
        }

        synchronized void increment() {
            LocalDateTime now = LocalDateTime.now();
            if (windowStart.plusSeconds(600).isBefore(now)) {
                windowStart = now;
                count = 1;
            } else {
                count++;
            }
        }

        synchronized long getCount(LocalDateTime now, long windowSeconds) {
            if (windowStart.plusSeconds(windowSeconds).isBefore(now)) {
                return 0;
            }
            return count;
        }
    }
}

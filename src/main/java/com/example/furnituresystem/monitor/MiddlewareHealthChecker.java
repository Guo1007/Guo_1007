package com.example.furnituresystem.monitor;

import com.example.furnituresystem.service.EmailService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
public class MiddlewareHealthChecker {

    @Resource
    private DataSource dataSource;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private MonitorProperties monitorProperties;

    @Resource
    private EmailService emailService;

    /**
     * 记录上次各组件状态，用于检测状态变更（从 UP 变 DOWN 才告警）
     */
    private final Map<String, Boolean> lastStatus = new HashMap<>();

    @PostConstruct
    public void onStartup() {
        log.info("==================== 中间件连接自检（启动） ====================");
        Map<String, String> results = checkAll();
        results.forEach((name, result) -> {
            log.info("  {} → {}", name, result);
            lastStatus.put(name, !result.startsWith("DOWN"));
        });
        log.info("============================================================");
    }

    @Scheduled(fixedDelay = 300_000, initialDelay = 60_000)
    public void periodicCheck() {
        Map<String, String> results = checkAll();
        List<String> newlyDown = new ArrayList<>();
        results.forEach((name, result) -> {
            boolean isUp = !result.startsWith("DOWN");
            Boolean wasUp = lastStatus.getOrDefault(name, true);
            if (wasUp && !isUp) {
                newlyDown.add(name + " → " + result);
            }
            lastStatus.put(name, isUp);
        });

        if (!newlyDown.isEmpty()) {
            log.error("==================== 中间件连接异常！ ====================");
            newlyDown.forEach(e -> log.error("  {}", e));
            log.error("============================================================");
            sendAlert(newlyDown);
        }
    }

    public Map<String, Object> health() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "UP");

        Map<String, Object> db = checkDb();
        status.put("database", db);
        if (!"UP".equals(db.get("status"))) status.put("status", "DEGRADED");

        Map<String, Object> redis = checkRedis();
        status.put("redis", redis);
        if (!"UP".equals(redis.get("status"))) status.put("status", "DEGRADED");

        Map<String, Object> mq = checkMq();
        status.put("rocketMQ", mq);
        if (!"UP".equals(mq.get("status"))) status.put("status", "DEGRADED");

        return status;
    }

    private void sendAlert(List<String> errors) {
        if (!monitorProperties.isAlertEnabled()) return;
        List<String> emails = monitorProperties.getAlertEmails();
        if (emails.isEmpty()) return;

        String body = "以下中间件连接异常：\n\n" + String.join("\n", errors)
                + "\n\n检测时间：" + LocalDateTime.now()
                + "\n请及时排查。";
        for (String to : emails) {
            emailService.sendNotificationEmail(to, "中间件异常告警", body);
        }
    }

    private Map<String, String> checkAll() {
        Map<String, String> results = new LinkedHashMap<>();
        results.put("数据库", checkDbBrief());
        results.put("Redis", checkRedisBrief());
        results.put("RocketMQ", checkMqBrief());
        return results;
    }

    private String checkDbBrief() {
        try (Connection conn = dataSource.getConnection()) {
            return "UP (" + conn.getMetaData().getDatabaseProductName() + ")";
        } catch (Exception e) {
            return "DOWN (" + e.getMessage() + ")";
        }
    }

    private String checkRedisBrief() {
        try {
            String pong = stringRedisTemplate.getConnectionFactory().getConnection().ping();
            return "UP".equals(pong) ? "UP" : "DOWN";
        } catch (Exception e) {
            return "DOWN (" + e.getMessage() + ")";
        }
    }

    private String checkMqBrief() {
        try {
            String addr = rocketMQTemplate.getProducer().getNamesrvAddr();
            return "UP (namesrv=" + addr + ")";
        } catch (Exception e) {
            return "DOWN (" + e.getMessage() + ")";
        }
    }

    private Map<String, Object> checkDb() {
        Map<String, Object> s = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            s.put("status", "UP");
            s.put("database", conn.getMetaData().getDatabaseProductName());
            s.put("url", conn.getMetaData().getURL());
        } catch (Exception e) {
            s.put("status", "DOWN");
            s.put("error", e.getMessage());
        }
        return s;
    }

    private Map<String, Object> checkRedis() {
        Map<String, Object> s = new LinkedHashMap<>();
        try {
            String pong = stringRedisTemplate.getConnectionFactory().getConnection().ping();
            s.put("status", "UP".equals(pong) ? "UP" : "DOWN");
            s.put("ping", pong);
        } catch (Exception e) {
            s.put("status", "DOWN");
            s.put("error", e.getMessage());
        }
        return s;
    }

    private Map<String, Object> checkMq() {
        Map<String, Object> s = new LinkedHashMap<>();
        try {
            String addr = rocketMQTemplate.getProducer().getNamesrvAddr();
            s.put("status", "UP");
            s.put("nameServer", addr);
        } catch (Exception e) {
            s.put("status", "DOWN");
            s.put("error", e.getMessage());
        }
        return s;
    }
}

package com.example.furnituresystem.monitor;

import com.example.furnituresystem.entity.dto.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/monitor")
public class HealthController {

    @Resource
    private DataSource dataSource;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private ErrorLogCollector errorLogCollector;

    @GetMapping("/health")
    public Result health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        // 检查数据库
        Map<String, Object> dbStatus = checkDatabase();
        status.put("database", dbStatus);
        if (!"UP".equals(dbStatus.get("status"))) {
            status.put("status", "DEGRADED");
        }
        // 检查 Redis
        Map<String, Object> redisStatus = checkRedis();
        status.put("redis", redisStatus);
        if (!"UP".equals(redisStatus.get("status"))) {
            status.put("status", "DEGRADED");
        }
        // 检查 RocketMQ
        Map<String, Object> mqStatus = checkRocketMQ();
        status.put("rocketMQ", mqStatus);
        if (!"UP".equals(mqStatus.get("status"))) {
            status.put("status", "DEGRADED");
        }
        // 系统信息
        status.put("system", getSystemInfo());

        return Result.ok(status);
    }

    @GetMapping("/errors")
    public Result getRecentErrors() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalErrors", errorLogCollector.getTotalErrorCount());
        result.put("stats", errorLogCollector.getErrorStats());
        result.put("recent", errorLogCollector.getRecentErrors(20));
        return Result.ok(result);
    }

    private Map<String, Object> checkDatabase() {
        Map<String, Object> status = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            status.put("status", "UP");
            status.put("database", conn.getMetaData().getDatabaseProductName());
            status.put("url", conn.getMetaData().getURL());
            log.info("[健康检查] 数据库: UP ({})", conn.getMetaData().getDatabaseProductName());
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
            log.error("[健康检查] 数据库: DOWN ({})", e.getMessage());
        }
        return status;
    }

    private Map<String, Object> checkRedis() {
        Map<String, Object> status = new HashMap<>();
        try {
            String pong = stringRedisTemplate.getConnectionFactory().getConnection().ping();
            status.put("status", "UP".equals(pong) ? "UP" : "DOWN");
            status.put("ping", pong);
            log.info("[健康检查] Redis: {}", "UP".equals(pong) ? "UP" : "DOWN");
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
            log.error("[健康检查] Redis: DOWN ({})", e.getMessage());
        }
        return status;
    }

    private Map<String, Object> checkRocketMQ() {
        Map<String, Object> status = new HashMap<>();
        try {
            String namesrvAddr = rocketMQTemplate.getProducer().getNamesrvAddr();
            status.put("status", "UP");
            status.put("nameServer", namesrvAddr);
            log.info("[健康检查] RocketMQ: UP (namesrv={})", namesrvAddr);
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
            log.error("[健康检查] RocketMQ: DOWN ({})", e.getMessage());
        }
        return status;
    }

    private Map<String, Object> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

        info.put("os", os.getName() + " " + os.getVersion());
        info.put("arch", os.getArch());
        info.put("availableProcessors", os.getAvailableProcessors());
        info.put("uptime", runtime.getUptime() / 1000 + "s");
        info.put("heapMemoryUsage", memory.getHeapMemoryUsage().getUsed() / 1024 / 1024 + "MB");
        info.put("nonHeapMemoryUsage", memory.getNonHeapMemoryUsage().getUsed() / 1024 / 1024 + "MB");
        return info;
    }
}

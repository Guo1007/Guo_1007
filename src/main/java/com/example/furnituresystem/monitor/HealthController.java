package com.example.furnituresystem.monitor;

import com.example.furnituresystem.entity.dto.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 监控接口控制器。
 * <ul>
 *   <li>{@code GET /monitor/health} — 中间件连接状态 + 系统信息</li>
 *   <li>{@code GET /monitor/errors} — 最近业务异常记录</li>
 * </ul>
 */
@RestController
@RequestMapping("/monitor")
public class HealthController {

    @Resource
    private MiddlewareHealthChecker healthChecker;

    @Resource
    private ErrorLogCollector errorLogCollector;

    /**
     * 健康检查：返回数据库 / Redis / RocketMQ 连接状态及系统资源信息
     */
    @GetMapping("/health")
    public Result health() {
        Map<String, Object> status = healthChecker.health();
        status.put("timestamp", System.currentTimeMillis());
        status.put("system", getSystemInfo());
        return Result.ok(status);
    }

    /** 异常日志：返回总数、分组统计、最近 20 条异常记录 */
    @GetMapping("/errors")
    public Result getRecentErrors() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalErrors", errorLogCollector.getTotalErrorCount());
        result.put("stats", errorLogCollector.getErrorStats());
        result.put("recent", errorLogCollector.getRecentErrors(20));
        return Result.ok(result);
    }

    private Map<String, Object> getSystemInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
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

package com.example.furnituresystem.monitor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 业务异常收集器。
 * 由 {@link com.example.furnituresystem.config.MyExceptionHandler} 在捕获异常时写入，
 * 供 /monitor/errors 接口查询最近的异常记录和分类统计。
 */
@Slf4j
@Component
public class ErrorLogCollector {

    /**
     * 最多保留最近 N 条异常记录
     */
    private static final int MAX_ERRORS = 500;

    private final ConcurrentLinkedDeque<ErrorRecord> errorQueue = new ConcurrentLinkedDeque<>();
    private final AtomicLong errorCount = new AtomicLong(0);

    /** 记录一条异常（由全局异常处理器调用） */
    public void record(Throwable e, String requestUri) {
        ErrorRecord record = new ErrorRecord();
        record.setTime(LocalDateTime.now());
        record.setType(e.getClass().getSimpleName());
        record.setMessage(e.getMessage() != null ? truncate(e.getMessage(), 200) : "");
        record.setStackTrace(truncate(stackTrace(e), 1000));
        record.setRequestUri(requestUri != null ? requestUri : "");
        record.setId(errorCount.incrementAndGet());

        errorQueue.addFirst(record);
        if (errorQueue.size() > MAX_ERRORS) {
            errorQueue.pollLast();
        }
    }

    /** 获取最近 N 条异常（最多 100） */
    public List<ErrorRecord> getRecentErrors(int limit) {
        return errorQueue.stream().limit(Math.min(limit, 100)).collect(Collectors.toList());
    }

    /** 按异常类型分组统计，按次数降序 */
    public List<ErrorStat> getErrorStats() {
        Map<String, Long> stats = new HashMap<>();
        Map<String, ErrorRecord> latest = new HashMap<>();
        for (ErrorRecord r : errorQueue) {
            stats.merge(r.getType(), 1L, Long::sum);
            latest.putIfAbsent(r.getType(), r);
        }
        return stats.entrySet().stream()
                .map(e -> {
                    ErrorStat stat = new ErrorStat();
                    stat.setType(e.getKey());
                    stat.setCount(e.getValue());
                    ErrorRecord last = latest.get(e.getKey());
                    if (last != null) {
                        stat.setLastTime(last.getTime());
                        stat.setLastMessage(last.getMessage());
                    }
                    return stat;
                })
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());
    }

    public long getTotalErrorCount() {
        return errorCount.get();
    }

    /** 截取异常栈前 N 字符 */
    private static String stackTrace(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("\n");
        for (StackTraceElement el : e.getStackTrace()) {
            sb.append("\tat ").append(el).append("\n");
            if (sb.length() > 1500) {
                sb.append("\t...");
                break;
            }
        }
        return sb.toString();
    }

    private static String truncate(String s, int maxLen) {
        return s != null && s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }

    @Data
    public static class ErrorRecord {
        private long id;
        private LocalDateTime time;
        /** 异常类名 */
        private String type;
        /** 异常消息（截断） */
        private String message;
        /** 异常栈（截断） */
        private String stackTrace;
        /** 触发异常的请求路径 */
        private String requestUri;

        public String getFormattedTime() {
            return time != null ? time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
        }
    }

    @Data
    public static class ErrorStat {
        private String type;
        private long count;
        private LocalDateTime lastTime;
        private String lastMessage;
    }
}

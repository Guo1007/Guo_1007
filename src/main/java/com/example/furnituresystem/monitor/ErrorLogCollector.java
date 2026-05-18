package com.example.furnituresystem.monitor;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorLogCollector {

    private static final int MAX_ERRORS = 500;
    private final ConcurrentLinkedDeque<ErrorRecord> errorQueue = new ConcurrentLinkedDeque<>();
    private final AtomicLong errorCount = new AtomicLong(0);
    private final AlertService alertService;

    @PostConstruct
    public void init() {
        log.info("ErrorLogCollector initialized, max errors: {}", MAX_ERRORS);
    }

    public void record(Throwable e, String requestUri) {
        ErrorRecord record = new ErrorRecord();
        record.setTime(LocalDateTime.now());
        record.setType(e.getClass().getSimpleName());
        record.setMessage(e.getMessage() != null ? truncate(e.getMessage(), 200) : "");
        record.setStackTrace(truncate(getStackTrace(e), 1000));
        record.setRequestUri(requestUri != null ? requestUri : "");
        record.setId(errorCount.incrementAndGet());

        errorQueue.addFirst(record);
        if (errorQueue.size() > MAX_ERRORS) {
            errorQueue.pollLast();
        }

        alertService.onError(record);
    }

    /** 获取最近的错误记录 */
    public List<ErrorRecord> getRecentErrors(int limit) {
        return errorQueue.stream().limit(Math.min(limit, 100)).collect(Collectors.toList());
    }

    /** 按类型统计错误数量 */
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

    /** 获取总错误数 */
    public long getTotalErrorCount() {
        return errorCount.get();
    }

    private static String getStackTrace(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("\n");
        for (StackTraceElement el : e.getStackTrace()) {
            sb.append("\tat ").append(el.toString()).append("\n");
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
        private String type;
        private String message;
        private String stackTrace;
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

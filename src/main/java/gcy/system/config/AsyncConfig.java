package gcy.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("cacheRebuildExecutor")
    public Executor cacheRebuildExecutor() {
        AtomicInteger counter = new AtomicInteger(0);
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r);
            thread.setName("cache-rebuild-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
        return new ThreadPoolExecutor(
                2,
                10,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}

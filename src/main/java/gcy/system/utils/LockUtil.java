package gcy.system.utils;

import gcy.system.entity.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redisson 分布式锁工具，收敛 tryLock/finally/unlock 样板。
 */
@Slf4j
public final class LockUtil {

    private LockUtil() {
    }

    public static Result executeWithLock(RedissonClient redissonClient, String lockKey,
                                         long waitTimeSeconds, Supplier<Result> action) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitTimeSeconds, TimeUnit.SECONDS);
            if (locked) {
                return action.get();
            }
            return Result.fail("操作处理中，请勿重复提交");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取锁被中断: lockKey={}", lockKey, e);
            return Result.fail("系统繁忙，请稍后重试");
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }
}

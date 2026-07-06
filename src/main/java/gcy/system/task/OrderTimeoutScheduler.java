package gcy.system.task;

import gcy.system.mapper.OrderMapper;
import gcy.system.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static gcy.system.utils.RedisConstants.ORDER_TIMEOUT_TASK_KEY;

/**
 * 未支付订单超时自动取消调度器。
 * 每分钟扫描一次超过 {@code order.payment-timeout-minutes} 分钟仍未支付的订单，
 * 自动取消并释放库存。使用 Redisson 分布式锁确保多实例环境下只有一个实例执行。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OrderMapper orderMapper;

    private final IOrderService orderService;

    private final RedissonClient redissonClient;

    @Value("${order.payment-timeout-minutes:1440}")
    private int paymentTimeoutMinutes;

    /**
     * 每分钟执行一次超时订单扫描。
     */
    @Scheduled(cron = "0 * * * * *")
    public void cancelTimeoutOrders() {
        RLock lock = redissonClient.getLock(ORDER_TIMEOUT_TASK_KEY);
        boolean locked = false;
        try {
            // tryLock(0, ...)：抢不到立刻放弃，不阻塞，下一分钟再试
            locked = lock.tryLock(0, 60, TimeUnit.SECONDS);
            if (!locked) {
                return;
            }
            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(paymentTimeoutMinutes);
            List<Long> timeoutOrderIds = orderMapper.selectTimeoutOrders(cutoff);
            if (timeoutOrderIds.isEmpty()) {
                return;
            }
            log.info("发现 {} 个超时未支付订单，准备自动取消，超时阈值: {} 分钟", timeoutOrderIds.size(), paymentTimeoutMinutes);
            int cancelled = 0;
            int failed = 0;
            for (Long orderId : timeoutOrderIds) {
                try {
                    var result = orderService.cancelTimeoutOrder(orderId);
                    if (result.getSuccess()) {
                        cancelled++;
                    } else {
                        failed++;
                        log.warn("自动取消订单失败: orderId={}, errorMsg={}", orderId, result.getErrorMsg());
                    }
                } catch (Exception e) {
                    failed++;
                    log.error("自动取消订单异常: orderId={}", orderId, e);
                }
            }
            log.info("超时订单自动取消完成: 成功={}, 失败={}, 总数={}", cancelled, failed, timeoutOrderIds.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("超时取消定时任务获取锁被中断", e);
        } catch (Exception e) {
            log.error("超时取消定时任务执行失败", e);
        } finally {
            if (locked) {
                try {
                    lock.unlock();
                } catch (Exception ignored) {
                }
            }
        }
    }
}

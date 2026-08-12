package gcy.system.task;

import gcy.system.entity.dto.StockAlertItem;
import gcy.system.entity.vo.LowStockVO;
import gcy.system.mapper.FurnitureMapper;
import gcy.system.service.admin.AdminNotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static gcy.system.utils.RedisConstants.STOCK_ALERT_TASK_KEY;

/**
 * 低库存预警调度器。
 * <p>
 * 每天上午10点和下午6点自动扫描库存不足的商品，按后台「库存预警」通知配置的开关与接收人发送预警邮件。
 * 使用 Redisson 分布式锁确保多实例环境下只有一个实例执行。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockAlertScheduler {

    private final FurnitureMapper furnitureMapper;

    private final AdminNotifyService adminNotifyService;

    private final RedissonClient redissonClient;

    @Scheduled(cron = "0 0 10,18 * * *", zone = "Asia/Shanghai")
    public void checkLowStock() {
        RLock lock = redissonClient.getLock(STOCK_ALERT_TASK_KEY);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 60, TimeUnit.SECONDS);
            if (!locked) {
                return;
            }
            List<LowStockVO> lowStockItems = furnitureMapper.selectLowStock();
            if (lowStockItems.isEmpty()) {
                return;
            }
            List<StockAlertItem> alertItems = lowStockItems.stream()
                    .map(item -> new StockAlertItem(item.getFName(), item.getStock()))
                    .collect(Collectors.toList());

            // 邮件仅展示前 15 条，超出部分在邮件底部汇总提示总数
            int totalCount = alertItems.size();
            List<StockAlertItem> displayItems = alertItems.size() > 15
                    ? alertItems.subList(0, 15)
                    : alertItems;

            // 开关与接收人的判断及发送统一交给 AdminNotifyService
            adminNotifyService.sendStockAlert(displayItems, totalCount);
            log.info("库存预警完成，涉及 {} 件商品", lowStockItems.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("库存预警获取锁被中断", e);
        } catch (Exception e) {
            log.error("库存预警检查失败", e);
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
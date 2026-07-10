package gcy.system.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import gcy.system.entity.dto.StockAlertItem;
import gcy.system.entity.pojo.User;
import gcy.system.entity.vo.LowStockVO;
import gcy.system.mapper.FurnitureMapper;
import gcy.system.mapper.UserMapper;
import gcy.system.service.EmailService;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class StockAlertScheduler {

    private final FurnitureMapper furnitureMapper;

    private final UserMapper userMapper;

    private final EmailService emailService;

    private final RedissonClient redissonClient;

    @Scheduled(cron = "0 0 10,18 * * *")
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

            // 直接向管理员发送预警邮件
            List<User> admins = userMapper.selectList(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getIsAdmin, 1)
                            .isNotNull(User::getEmail)
                            .ne(User::getEmail, ""));
            if (admins.isEmpty()) {
                log.warn("库存预警：没有已绑定邮箱的管理员");
                return;
            }
            for (User admin : admins) {
                emailService.sendStockAlertEmail(admin.getEmail(), "库存预警", alertItems);
                log.debug("库存预警邮件已发送至管理员: {}", admin.getEmail());
            }
            log.info("库存预警完成，涉及 {} 件商品，通知 {} 位管理员", lowStockItems.size(), admins.size());
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

package gcy.system.task;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import gcy.system.entity.dto.StockAlertItem;
import gcy.system.entity.pojo.AdminNotifySetting;
import gcy.system.entity.pojo.User;
import gcy.system.entity.vo.LowStockVO;
import gcy.system.mapper.AdminNotifySettingMapper;
import gcy.system.mapper.FurnitureMapper;
import gcy.system.mapper.UserMapper;
import gcy.system.integration.EmailService;
import gcy.system.service.admin.Impl.NotifySettingServiceImpl;
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

    private final AdminNotifySettingMapper adminNotifySettingMapper;

    private final UserMapper userMapper;

    private final EmailService emailService;

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

            // 读取后台「库存预警」通知配置：开关 + 接收管理员ID
            AdminNotifySetting setting = adminNotifySettingMapper.selectOne(
                    new LambdaQueryWrapper<AdminNotifySetting>()
                            .eq(AdminNotifySetting::getNotifyType, NotifySettingServiceImpl.TYPE_STOCK_ALERT));
            if (setting == null) {
                log.warn("未找到库存预警通知配置，可能未执行 admin_notify_setting 迁移，跳过");
                return;
            }
            if (setting.getEnabled() == null || setting.getEnabled() != 1) {
                log.debug("库存预警通知未开启，跳过");
                return;
            }
            List<Long> adminIds = NotifySettingServiceImpl.parseIds(setting.getAdminIds());
            if (adminIds.isEmpty()) {
                log.debug("库存预警未配置接收管理员，跳过");
                return;
            }
            List<User> admins = userMapper.selectBatchIds(adminIds);
            if (admins == null || admins.isEmpty()) {
                return;
            }
            int sentCount = 0;
            for (User admin : admins) {
                if (admin.getIsAdmin() != null && admin.getIsAdmin() == 1
                        && StrUtil.isNotBlank(admin.getEmail())) {
                    emailService.sendStockAlertEmail(admin.getEmail(), "库存预警", displayItems, totalCount);
                    log.debug("库存预警邮件已发送至管理员: {}", admin.getEmail());
                    sentCount++;
                }
            }
            if (sentCount == 0) {
                log.warn("库存预警配置了接收人但未发出邮件，可能接收人被降级或未绑定邮箱，adminIds={}", adminIds);
            }
            log.info("库存预警完成，涉及 {} 件商品，通知 {} 位管理员", lowStockItems.size(), sentCount);
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

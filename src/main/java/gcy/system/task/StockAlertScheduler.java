package gcy.system.task;

import cn.hutool.json.JSONUtil;
import gcy.system.entity.dto.RocketMQMessage;
import gcy.system.entity.dto.StockAlertItem;
import gcy.system.entity.vo.LowStockVO;
import gcy.system.mapper.FurnitureMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
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

    private final RocketMQTemplate rocketMQTemplate;

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

            RocketMQMessage msg = new RocketMQMessage();
            msg.setType("stock-alert");
            msg.setTitle("库存预警");
            msg.setContent(JSONUtil.toJsonStr(alertItems));
            rocketMQTemplate.convertAndSend("stock-alert-topic", JSONUtil.toJsonStr(msg));
            log.info("库存预警消息已发送，涉及 {} 件商品", lowStockItems.size());
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

package gcy.system.task;

import cn.hutool.json.JSONUtil;
import gcy.system.entity.dto.RocketMQMessage;
import gcy.system.entity.dto.StockAlertItem;
import gcy.system.mapper.FurnitureMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockAlertScheduler {

    private final FurnitureMapper furnitureMapper;

    private final RocketMQTemplate rocketMQTemplate;

    @Scheduled(cron = "0 0 10,18 * * *")
    public void checkLowStock() {
        try {
            List<Map<String, Object>> lowStockItems = furnitureMapper.selectLowStock();
            if (lowStockItems.isEmpty()) {
                return;
            }
            // 转换为DTO列表
            List<StockAlertItem> alertItems = lowStockItems.stream()
                    .map(item -> new StockAlertItem(
                            item.get("f_name") != null ? item.get("f_name").toString() : "未知商品",
                            item.get("stock") != null ? Integer.parseInt(item.get("stock").toString()) : 0
                    ))
                    .collect(Collectors.toList());

            RocketMQMessage msg = new RocketMQMessage();
            msg.setType("stock-alert");
            msg.setTitle("库存预警");
            msg.setContent(JSONUtil.toJsonStr(alertItems));
            rocketMQTemplate.convertAndSend("stock-alert-topic", JSONUtil.toJsonStr(msg));
            log.info("库存预警消息已发送，涉及 {} 件商品", lowStockItems.size());
        } catch (Exception e) {
            log.error("库存预警检查失败", e);
        }
    }
}

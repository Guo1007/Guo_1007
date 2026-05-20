package com.example.furnituresystem.service;

import cn.hutool.json.JSONUtil;
import com.example.furnituresystem.entity.dto.RocketMQMessage;
import com.example.furnituresystem.mapper.FurnitureMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StockAlertScheduler {

    @Resource
    private FurnitureMapper furnitureMapper;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Scheduled(fixedDelay = 600_000)
    public void checkLowStock() {
        try {
            List<Map<String, Object>> lowStockItems = furnitureMapper.selectLowStock();
            if (lowStockItems.isEmpty()) {
                return;
            }
            String itemList = lowStockItems.stream()
                    .map(m -> m.get("fName") + "（库存: " + m.get("stock") + "）")
                    .collect(Collectors.joining("、"));
            RocketMQMessage msg = new RocketMQMessage();
            msg.setType("stock-alert");
            msg.setTitle("库存预警");
            msg.setContent("以下商品库存不足 10 件：" + itemList + "，请及时补货。");
            rocketMQTemplate.convertAndSend("stock-alert-topic", JSONUtil.toJsonStr(msg));
            log.info("库存预警消息已发送，涉及 {} 件商品", lowStockItems.size());
        } catch (Exception e) {
            log.error("库存预警检查失败", e);
        }
    }
}

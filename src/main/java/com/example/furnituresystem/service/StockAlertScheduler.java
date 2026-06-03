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

@Slf4j
@Component
public class StockAlertScheduler {

    @Resource
    private FurnitureMapper furnitureMapper;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Scheduled(cron = "0 0 10,18 * * *")
    public void checkLowStock() {
        try {
            List<Map<String, Object>> lowStockItems = furnitureMapper.selectLowStock();
            if (lowStockItems.isEmpty()) {
                return;
            }
            String html = buildStockAlertHtml(lowStockItems);
            RocketMQMessage msg = new RocketMQMessage();
            msg.setType("stock-alert");
            msg.setTitle("库存预警");
            msg.setContent(html);
            rocketMQTemplate.convertAndSend("stock-alert-topic", JSONUtil.toJsonStr(msg));
            log.info("库存预警消息已发送，涉及 {} 件商品", lowStockItems.size());
        } catch (Exception e) {
            log.error("库存预警检查失败", e);
        }
    }

    private String buildStockAlertHtml(List<Map<String, Object>> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family: 'Microsoft YaHei', Arial, sans-serif; max-width: 600px; margin: 0 auto;\">")
                .append("<h3 style=\"color: #e74c3c; border-bottom: 2px solid #e74c3c; padding-bottom: 8px;\">")
                .append("⚠️ 库存预警</h3>")
                .append("<p>以下商品<strong>库存不足 10 件</strong>，请及时补货：</p>")
                .append("<table style=\"width: 100%; border-collapse: collapse; margin-top: 12px;\">")
                .append("<thead><tr style=\"background: #f8d7da; color: #721c24;\">")
                .append("<th style=\"padding: 10px; border: 1px solid #f5c6cb; text-align: left;\">商品名称</th>")
                .append("<th style=\"padding: 10px; border: 1px solid #f5c6cb; text-align: center; width: 80px;\">当前库存</th>")
                .append("</tr></thead><tbody>");
        for (Map<String, Object> item : items) {
            Object name = item.get("f_name");
            Object stock = item.get("stock");
            sb.append("<tr style=\"background: #fff3f3;\">")
                    .append("<td style=\"padding: 8px 10px; border: 1px solid #f5c6cb;\">")
                    .append(name != null ? name : "未知商品")
                    .append("</td>")
                    .append("<td style=\"padding: 8px 10px; border: 1px solid #f5c6cb; text-align: center; font-weight: bold; color: #e74c3c;\">")
                    .append(stock != null ? stock : "?")
                    .append("</td></tr>");
        }
        sb.append("</tbody></table>")
                .append("<p style=\"margin-top: 16px; color: #999; font-size: 12px;\">")
                .append("此邮件由家具商城系统自动发送，请勿回复。<br>")
                .append("发送时间：")
                .append(java.time.LocalDateTime.now())
                .append("</p></div>");
        return sb.toString();
    }
}

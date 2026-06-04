package com.Guo.furnituresystem.listener;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.Guo.furnituresystem.entity.dto.RocketMQMessage;
import com.Guo.furnituresystem.service.EmailService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "order-status-topic", consumerGroup = "order-status-consumer")
public class OrderStatusListener implements RocketMQListener<String> {

    @Resource
    private EmailService emailService;

    @Override
    public void onMessage(String message) {
        try {
            RocketMQMessage msg = JSONUtil.toBean(message, RocketMQMessage.class);
            if (StrUtil.isBlank(msg.getUserEmail())) {
                log.warn("订单状态消息缺少邮箱: orderId={}, type={}", msg.getOrderId(), msg.getType());
                return;
            }
            emailService.sendNotificationEmail(msg.getUserEmail(), msg.getTitle(), msg.getContent());
            log.info("订单状态邮件已发送: orderId={}, type={}, email={}", msg.getOrderId(), msg.getType(), msg.getUserEmail());
        } catch (Exception e) {
            log.error("处理订单状态消息失败: {}", message, e);
        }
    }
}

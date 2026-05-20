package com.example.furnituresystem.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.furnituresystem.entity.dto.RocketMQMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "notification-email-topic", consumerGroup = "notification-email-consumer")
public class NotificationEmailListener implements RocketMQListener<String> {

    @Resource
    private EmailService emailService;

    @Override
    public void onMessage(String message) {
        try {
            RocketMQMessage msg = JSONUtil.toBean(message, RocketMQMessage.class);
            if (StrUtil.isBlank(msg.getUserEmail())) {
                log.warn("通知邮件消息缺少邮箱: userId={}", msg.getUserId());
                return;
            }
            emailService.sendNotificationEmail(msg.getUserEmail(), msg.getTitle(), msg.getContent());
            log.info("通知邮件已发送: userId={}, email={}", msg.getUserId(), msg.getUserEmail());
        } catch (Exception e) {
            log.error("处理通知邮件消息失败: {}", message, e);
        }
    }
}

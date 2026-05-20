package com.example.furnituresystem.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.furnituresystem.entity.dto.RocketMQMessage;
import com.example.furnituresystem.entity.pojo.User;
import com.example.furnituresystem.mapper.UserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RocketMQConsumerService {

    @Resource
    private EmailService emailService;

    @Resource
    private UserMapper userMapper;

    @Component
    @RocketMQMessageListener(topic = "order-status-topic", consumerGroup = "furniture-consumer")
    public class OrderStatusListener implements RocketMQListener<String> {
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

    @Component
    @RocketMQMessageListener(topic = "notification-email-topic", consumerGroup = "furniture-consumer")
    public class NotificationEmailListener implements RocketMQListener<String> {
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

    @Component
    @RocketMQMessageListener(topic = "stock-alert-topic", consumerGroup = "furniture-consumer")
    public class StockAlertListener implements RocketMQListener<String> {
        @Override
        public void onMessage(String message) {
            try {
                RocketMQMessage msg = JSONUtil.toBean(message, RocketMQMessage.class);
                LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(User::getIsAdmin, 1)
                        .isNotNull(User::getEmail)
                        .ne(User::getEmail, "");
                java.util.List<User> admins = userMapper.selectList(wrapper);
                if (admins.isEmpty()) {
                    log.warn("库存预警：没有已绑定邮箱的管理员");
                    return;
                }
                for (User admin : admins) {
                    emailService.sendNotificationEmail(admin.getEmail(), msg.getTitle(), msg.getContent());
                    log.info("库存预警邮件已发送至管理员: {}", admin.getEmail());
                }
            } catch (Exception e) {
                log.error("处理库存预警消息失败: {}", message, e);
            }
        }
    }
}

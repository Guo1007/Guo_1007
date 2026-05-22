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

import java.util.List;

/**
 * 通知邮件消费者。
 * <ul>
 *   <li>指定用户通知：消息中含 userEmail，直接发邮件</li>
 *   <li>全体用户通知：消息中无具体用户（type=notification-all），查全量用户逐个发</li>
 * </ul>
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "notification-email-topic", consumerGroup = "notification-email-consumer")
public class NotificationEmailListener implements RocketMQListener<String> {

    @Resource
    private EmailService emailService;

    @Resource
    private UserMapper userMapper;

    @Override
    public void onMessage(String message) {
        try {
            RocketMQMessage msg = JSONUtil.toBean(message, RocketMQMessage.class);
            if (StrUtil.isBlank(msg.getUserEmail())) {
                List<User> allUsers = userMapper.selectList(
                        new LambdaQueryWrapper<User>()
                                .isNotNull(User::getEmail)
                                .ne(User::getEmail, ""));
                if (allUsers.isEmpty()) {
                    log.warn("全体通知邮件：没有已绑定邮箱的用户");
                    return;
                }
                for (User u : allUsers) {
                    emailService.sendNotificationEmail(u.getEmail(), msg.getTitle(), msg.getContent());
                }
                log.info("全体通知邮件已发送: 覆盖 {} 位用户", allUsers.size());
                return;
            }
            // 指定用户通知
            emailService.sendNotificationEmail(msg.getUserEmail(), msg.getTitle(), msg.getContent());
            log.info("通知邮件已发送: userId={}, email={}", msg.getUserId(), msg.getUserEmail());
        } catch (Exception e) {
            log.error("处理通知邮件消息失败: {}", message, e);
        }
    }
}

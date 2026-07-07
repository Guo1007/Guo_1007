package gcy.system.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import gcy.system.entity.dto.RocketMQMessage;
import gcy.system.entity.pojo.Order;
import gcy.system.entity.pojo.User;
import gcy.system.mapper.UserMapper;
import gcy.system.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

/**
 * 订单状态 MQ 发送工具，统一 sendMQ + 邮件回退模式。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMqHelper {

    private final RocketMQTemplate rocketMQTemplate;
    private final EmailService emailService;
    private final UserMapper userMapper;

    public void send(String topic, Order order, String type, String title, String content,
                     String statusIcon, String statusColor) {
        User user = userMapper.selectById(order.getUserId());
        if (user == null || StrUtil.isBlank(user.getEmail())) {
            return;
        }
        try {
            RocketMQMessage msg = new RocketMQMessage();
            msg.setType(type);
            msg.setOrderId(order.getId());
            msg.setUserId(order.getUserId());
            msg.setUserEmail(user.getEmail());
            msg.setUserName(user.getUserName());
            msg.setTitle(title);
            msg.setContent(content);
            msg.setStatusIcon(statusIcon);
            msg.setStatusColor(statusColor);
            rocketMQTemplate.convertAndSend(topic, JSONUtil.toJsonStr(msg));
            log.info("订单状态MQ消息已发送: orderId={}, type={}", order.getId(), type);
        } catch (Exception e) {
            log.error("MQ发送失败，回退到直接邮件发送: orderId={}", order.getId(), e);
            emailService.sendOrderStatusEmail(user.getEmail(), order.getId(), title, content,
                    statusIcon, statusColor, order.getTotalPrice().toString(), user.getUserName());
        }
    }
}

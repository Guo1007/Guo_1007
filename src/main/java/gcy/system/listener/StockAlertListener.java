package gcy.system.listener;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import gcy.system.entity.dto.RocketMQMessage;
import gcy.system.entity.dto.StockAlertItem;
import gcy.system.entity.pojo.User;
import gcy.system.mapper.UserMapper;
import gcy.system.service.EmailService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RocketMQMessageListener(topic = "stock-alert-topic", consumerGroup = "stock-alert-consumer")
public class StockAlertListener implements RocketMQListener<String> {

    @Resource
    private EmailService emailService;

    @Resource
    private UserMapper userMapper;

    @Override
    public void onMessage(String message) {
        try {
            RocketMQMessage msg = JSONUtil.toBean(message, RocketMQMessage.class);
            List<User> admins = userMapper.selectList(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getIsAdmin, 1)
                            .isNotNull(User::getEmail)
                            .ne(User::getEmail, ""));
            if (admins.isEmpty()) {
                log.warn("库存预警：没有已绑定邮箱的管理员");
                return;
            }
            // 解析库存预警项
            List<StockAlertItem> items = JSONUtil.toList(msg.getContent(), StockAlertItem.class);
            for (User admin : admins) {
                emailService.sendStockAlertEmail(admin.getEmail(), msg.getTitle(), items);
                log.info("库存预警邮件已发送至管理员: {}", admin.getEmail());
            }
        } catch (Exception e) {
            log.error("处理库存预警消息失败: {}", message, e);
        }
    }
}

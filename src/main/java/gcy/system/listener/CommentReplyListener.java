package gcy.system.listener;

import cn.hutool.json.JSONUtil;
import gcy.system.entity.pojo.Notification;
import gcy.system.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "comment-reply-topic", consumerGroup = "comment-reply-consumer")
public class CommentReplyListener implements RocketMQListener<String> {

    private final NotificationMapper notificationMapper;

    @Override
    public void onMessage(String message) {
        try {
            var msg = JSONUtil.toBean(message, CommentReplyMessage.class);
            // 保存站内通知
            Notification notification = new Notification();
            notification.setUserId(msg.getTargetUserId());
            notification.setTitle("收到新回复");
            notification.setContent(msg.getContent());
            notification.setType("comment_reply");
            notification.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(notification);
            log.info("评论回复通知已保存: userId={}, reviewId={}", msg.getTargetUserId(), msg.getReviewId());
        } catch (Exception e) {
            log.error("处理评论回复消息失败: {}", message, e);
        }
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CommentReplyMessage {
        private Long targetUserId;
        private Long reviewId;
        private Long replyUserId;
        private String replyUserName;
        private String content;
    }
}

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

/**
 * 评论回复通知监听器。
 * <p>
 * 监听 RocketMQ Topic "comment-reply-topic"，当有用户回复评论时，
 * 消费MQ消息并将通知保存到站内通知表，提醒被回复的用户。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "comment-reply-topic", consumerGroup = "comment-reply-consumer")
public class CommentReplyListener implements RocketMQListener<String> {

    /** 站内通知数据访问接口 */
    private final NotificationMapper notificationMapper;

    /**
     * 处理评论回复MQ消息。
     * <p>
     * 解析消息体，将被回复用户的ID、回复内容、关联的评价和商品信息
     * 封装为通知记录并存入数据库。
     * </p>
     *
     * @param message 包含评论回复信息的JSON格式MQ消息
     */
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
            notification.setReviewId(msg.getReviewId());
            notification.setGoodsId(msg.getGoodsId());
            notification.setReviewCommentId(msg.getReviewCommentId());
            notification.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(notification);
            log.info("评论回复通知已保存: userId={}, reviewId={}", msg.getTargetUserId(), msg.getReviewId());
        } catch (Exception e) {
            log.error("处理评论回复消息失败: {}", message, e);
            throw new RuntimeException("评论回复消息处理失败", e);
        }
    }

    /**
     * 评论回复MQ消息体。
     * <p>
     * 包含被回复用户ID、评价ID、商品ID、回复者信息等字段，
     * 用于在消费者端反序列化MQ消息。
     * </p>
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CommentReplyMessage {

        /** 被回复的目标用户ID */
        private Long targetUserId;

        /** 关联的评价ID */
        private Long reviewId;

        /** 关联的商品ID */
        private Long goodsId;

        /** 关联的评论ID */
        private Long reviewCommentId;

        /** 回复者用户ID */
        private Long replyUserId;

        /** 回复者用户昵称 */
        private String replyUserName;

        /** 回复消息内容 */
        private String content;
    }
}

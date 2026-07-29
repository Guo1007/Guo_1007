package gcy.system.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知视图对象，封装通知列表或通知详情页面展示所需的字段，映射数据库中的通知表（notification）。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationVO {

    /** 通知主键ID */
    private Long id;

    /** 接收通知的用户ID */
    private Long userId;

    /** 接收通知的用户名称 */
    private String userName;

    /** 通知标题 */
    private String title;

    /** 通知正文内容 */
    private String content;

    /** 通知类型（如评论通知、审核通知、商品通知等） */
    private String type;

    /** 关联的审核记录ID */
    private Long reviewId;

    /** 关联的商品ID */
    private Long goodsId;

    /** 关联的评论回复ID */
    private Long reviewCommentId;

    /** 是否已读（true: 已读, false: 未读） */
    private Boolean isRead;

    /** 通知创建时间 */
    private LocalDateTime createTime;
}

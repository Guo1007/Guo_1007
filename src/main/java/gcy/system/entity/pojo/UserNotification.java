package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户通知状态实体，映射 user_notification 表，记录每个用户对每条通知的已读/删除状态。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_notification")
public class UserNotification {

    @TableId(type = IdType.AUTO)
    /** 主键ID，自增 */
    private Long id;

    /** 关联的通知ID */
    private Long notificationId;

    /** 关联的用户ID */
    private Long userId;

    /** 是否已读：0-未读，1-已读 */
    private Integer isRead;

    /** 是否已删除：0-未删除，1-已删除 */
    private Integer isDeleted;

    /** 阅读时间 */
    private LocalDateTime readTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

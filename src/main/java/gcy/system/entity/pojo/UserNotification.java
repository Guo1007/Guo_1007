package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户通知状态表 — 记录每个用户对每条通知的已读/删除状态
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_notification")
public class UserNotification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long notificationId;

    private Long userId;

    /** 0=未读 1=已读 */
    private Integer isRead;

    /** 0=未删 1=已删 */
    private Integer isDeleted;

    private LocalDateTime readTime;

    private LocalDateTime updateTime;
}

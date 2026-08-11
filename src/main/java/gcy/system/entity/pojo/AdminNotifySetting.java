package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员邮件通知配置实体。
 * <p>
 * 按功能独立配置：每种通知类型一行（notify_type），各自控制是否发送邮件
 * （enabled）以及由哪些管理员接收（admin_ids 逗号分隔的用户ID）。
 * </p>
 *
 * @author 郭名城
 * @date 2026-08-08
 */
@Data
@NoArgsConstructor
@TableName("admin_notify_setting")
public class AdminNotifySetting {

    /** 配置ID（自增主键） */
    private Long id;

    /** 通知类型：new_order-新订单、refund-售后退款、stock_alert-库存预警 */
    private String notifyType;

    /** 是否开启该功能通知(0否1是) */
    private Integer enabled;

    /** 接收通知的管理员ID，逗号分隔 */
    private String adminIds;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

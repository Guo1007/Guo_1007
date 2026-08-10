package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员邮件通知配置实体。
 * <p>
 * 单行配置（id 固定为 1），控制是否向管理员发送订单/售后等通知邮件，
 * 以及具体由哪些管理员接收（admin_ids 逗号分隔的用户ID）。
 * </p>
 *
 * @author 郭名城
 * @date 2026-08-08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("admin_notify_setting")
public class AdminNotifySetting {

    /** 配置ID（固定为1） */
    private Long id;

    /** 是否开启通知(0否1是) */
    private Integer enabled;

    /** 接收通知的管理员ID，逗号分隔 */
    private String adminIds;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

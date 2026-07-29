package gcy.system.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送通知请求的 DTO，用于接收前端提交的通知表单数据（标题、内容、类型、目标用户等）。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationFormDTO {

    /** 接收通知的用户 ID，为空时表示面向所有用户 */
    private Long userId;

    @NotBlank(message = "通知标题不能为空")
    /** 通知标题 */
    private String title;

    @NotBlank(message = "通知内容不能为空")
    /** 通知正文内容 */
    private String content;

    /** 通知类型，默认为 "system" */
    private String type = "system";

    /** 是否同时向用户发送邮件通知 */
    private Boolean sendEmail = false;
}

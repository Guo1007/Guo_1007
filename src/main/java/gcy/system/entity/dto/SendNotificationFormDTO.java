package gcy.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "接收通知的用户ID，为空时表示面向所有用户")
    private Long userId;

    @Schema(description = "通知标题")
    @NotBlank(message = "通知标题不能为空")
    private String title;

    @Schema(description = "通知正文内容")
    @NotBlank(message = "通知内容不能为空")
    private String content;

    @Schema(description = "通知类型，默认为system")
    private String type = "system";

    @Schema(description = "是否同时向用户发送邮件通知")
    private Boolean sendEmail = false;
}

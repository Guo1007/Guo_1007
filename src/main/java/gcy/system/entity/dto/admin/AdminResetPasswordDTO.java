package gcy.system.entity.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 重置密码表单请求DTO，用于管理员重置指定用户的密码。
 *
 * @author 郭名城
 * @date 2026-08-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminResetPasswordDTO {

    @Schema(description = "用户ID")
    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Schema(description = "新密码")
    @NotBlank(message = "请输入重置后的密码")
    private String newPassword;
}
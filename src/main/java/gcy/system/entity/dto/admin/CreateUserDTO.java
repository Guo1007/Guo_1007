package gcy.system.entity.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 新增用户请求DTO。
 * <p>
 * 管理员后台创建新用户账号（普通用户/管理员）时提交的表单数据。
 * userName 非唯一索引，不参与唯一性校验；手机号与邮箱至少填写一项（唯一性在 Service 层按实际填写的项校验）。
 * </p>
 *
 * @author 郭名城
 * @date 2026-08-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度在2到20个字符")
    private String userName;

    @Schema(description = "手机号（与邮箱至少填写一项）")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "邮箱（与手机号至少填写一项）")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少6位")
    private String password;

    @Schema(description = "是否为管理员（0-普通用户，1-管理员）")
    private Integer isAdmin = 0;
}

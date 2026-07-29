package gcy.system.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册表单数据传输对象，用于接收前端提交的注册请求参数。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterFormDTO {

    /** 用户注册邮箱地址 */
    private String email;

    /** 邮箱验证码 */
    private String code;

    /** 登录密码 */
    private String password;

    /** 确认密码，需与密码一致 */
    private String confirmPassword;

}

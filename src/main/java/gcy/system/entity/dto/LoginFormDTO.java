package gcy.system.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录表单数据传输对象（DTO），用于承载前端登录请求中的账号、验证码和密码信息。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class LoginFormDTO {

    /** 用户登录账号（用户名、手机号或邮箱） */
    private String account;

    /** 登录验证码（短信验证码或图形验证码） */
    private String code;

    /** 用户登录密码 */
    private String passWord;

}

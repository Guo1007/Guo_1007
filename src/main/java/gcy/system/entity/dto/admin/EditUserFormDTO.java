package gcy.system.entity.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 编辑用户表单请求DTO，用于承载编辑用户时提交的表单数据。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditUserFormDTO {

    /** 用户ID */
    private Long id;

    /** 新密码 */
    private String newPassword;

    /** 是否为管理员（1-是，0-否） */
    private Integer isAdmin;
}
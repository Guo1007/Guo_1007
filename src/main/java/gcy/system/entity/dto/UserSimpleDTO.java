package gcy.system.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户简要信息数据传输对象，用于在服务层与控制器层之间传递用户的简化信息。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSimpleDTO {

    /** 用户唯一标识 */
    private Long id;

    /** 用户名 */
    private String userName;

    /** 用户电子邮箱 */
    private String email;
}

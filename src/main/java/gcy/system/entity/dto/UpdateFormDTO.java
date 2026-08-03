package gcy.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户更新表单数据传输对象，用于承载用户个人信息更新的请求数据。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFormDTO {

    /** 用户名 */
    @Schema(description = "用户名")
    private String userName;

    /** 电子邮箱 */
    @Schema(description = "电子邮箱")
    private String email;

    /** 地址 */
    @Schema(description = "地址")
    private String address;

    /** 收货人姓名 */
    @Schema(description = "收货人姓名")
    private String consignee;

    /** 收货人联系电话 */
    @Schema(description = "收货人联系电话")
    private String consigneePhone;

    /** 用户头像图标路径，默认为空字符串 */
    @Schema(description = "用户头像图标路径")
    private String icon = "";

}

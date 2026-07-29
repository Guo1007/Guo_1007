package gcy.system.entity.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 家具种类表单数据传输对象（DTO），用于接收和校验管理员提交的家具种类新增或编辑请求数据。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminFurnitureTypeFormDTO {

    /** 家具种类主键ID，编辑时传入，新增时可为空 */
    private Long id;

    /** 家具种类名称 */
    @NotNull(message = "请输入种类名称！")
    private String name;

    /** 家具种类描述信息 */
    @NotNull(message = "请适当输入描述！")
    private String title;

    /** 家具种类图标/图片地址 */
    @NotNull(message = "请上传种类图片！")
    private String icon;

    /** 创建时间 */
    private String createTime;

    /** 最后更新时间 */
    private String updateTime;

}

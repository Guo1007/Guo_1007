package gcy.system.entity.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminFurnitureTypeFormDTO {

    private Long id;

    @NotNull(message = "请输入种类名称！")
    private String name;

    @NotNull(message = "请适当输入描述！")
    private String title;

    @NotNull(message = "请上传种类图片！")
    private String icon;

    private String createTime;

    private String updateTime;

}

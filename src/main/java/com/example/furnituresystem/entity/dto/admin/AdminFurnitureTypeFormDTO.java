package com.example.furnituresystem.entity.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "请适当输入描述！")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotNull(message = "请上传种类图片！")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String icon;

    private String createTime;

    private String updateTime;

}

package com.example.furnituresystem.entity.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminFurnitureFormDTO {

    private Long id;

    @NotBlank(message = "家具名称不能为空")
    private String fName;

    @NotBlank(message = "封面图片不能为空")
    private String fIcon;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    @NotNull(message = "分类ID不能为空")
    @Min(value = 1, message = "分类ID必须大于0")
    private Long typeId;

    @NotNull(message = "请填写正确的库存数")
    @Min(value = 0, message = "库存数不能小于0")
    private Integer stock;

    @NotNull(message = "请输入家具品牌")
    private String brand;

    private String intro;

    private String images;
}
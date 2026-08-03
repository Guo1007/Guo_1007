package gcy.system.entity.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 管理员端家具表单数据传输对象（DTO），用于承载新增或编辑家具时的请求数据。
 * 包含家具的基本信息、价格、库存、品牌、介绍、详情图片及推荐标记等字段。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminFurnitureFormDTO {

    @Schema(description = "家具ID，编辑时传入，新增时可为空")
    private Long id;

    @Schema(description = "家具名称")
    @NotBlank(message = "家具名称不能为空")
    private String fName;

    @Schema(description = "封面图片地址")
    @NotBlank(message = "封面图片不能为空")
    private String fIcon;

    @Schema(description = "家具价格")
    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    @Schema(description = "所属分类ID")
    @NotNull(message = "分类ID不能为空")
    @Min(value = 1, message = "分类ID必须大于0")
    private Long typeId;

    @Schema(description = "库存数量")
    @NotNull(message = "请填写正确的库存数")
    @Min(value = 0, message = "库存数不能小于0")
    private Integer stock;

    @Schema(description = "家具品牌")
    @NotNull(message = "请输入家具品牌")
    private String brand;

    @Schema(description = "家具简介")
    private String intro;

    @Schema(description = "家具详情图片，多个图片地址以逗号分隔")
    private String images;

    @Schema(description = "家具详细描述（富文本）")
    private String description;

    @Schema(description = "是否推荐，1表示推荐，0或null表示不推荐")
    private Integer isRecommended;
}
package gcy.system.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 低库存视图对象，用于展示库存不足的物品信息。
 * 包含物品的基本标识、名称、图标及当前库存数量。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LowStockVO {

    /** 物品唯一标识 */
    private Long id;

    @JsonProperty("fName")
    /** 物品名称 */
    private String fName;

    @JsonProperty("fIcon")
    /** 物品图标路径或图标标识 */
    private String fIcon;

    /** 当前库存数量 */
    private Integer stock;

    /** 分类ID */
    private Long typeId;

    /** 分类名称 */
    private String typeName;
}

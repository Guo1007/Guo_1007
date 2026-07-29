package gcy.system.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热门家具视图对象，用于展示销量排名靠前的家具汇总数据。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopFurnitureVO {

    /** 家具ID */
    private Long furnitureId;

    /** 家具名称 */
    private String furnitureName;

    /** 家具图标URL或图标标识 */
    private String furnitureIcon;

    /** 累计销售数量 */
    private long totalSold;
}

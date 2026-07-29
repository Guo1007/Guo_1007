package gcy.system.entity.pojo;


import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单项实体类，映射 order_item 表，记录订单中每个家具商品的明细信息，
 * 包括所购家具、SKU规格、单价、数量及该项小计金额。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {

    /** 主键ID */
    private Long id;

    /** 所属订单ID */
    private Long orderId;

    /** 家具ID */
    private Long furnitureId;

    /** SKU ID */
    private Long skuId;

    /** 家具名称 */
    private String furnitureName;

    /** 家具图标URL */
    private String furnitureIcon;

    /** 单价 */
    private BigDecimal price;

    /** 购买数量 */
    private int quantity;

    /** SKU规格描述 */
    private String skuSpec;

    /** 该项小计金额（单价 × 数量） */
    private BigDecimal itemTotalPrice;

    /** 逻辑删除标记（0=正常，1=已删除） */
    @TableLogic
    private Integer deleted = 0;

}

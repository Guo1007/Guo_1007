package gcy.system.entity.vo;

import cn.hutool.core.bean.BeanUtil;
import gcy.system.entity.pojo.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单项视图对象，用于展示订单中单个商品条目的详细信息，包括家具名称、图标、
 * 单价、数量、SKU规格及小计金额等。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemVO {

    /**
     * 订单项唯一标识
     */
    private String id;

    /**
     * 所属订单ID
     */
    private String orderId;

    /**
     * 家具商品ID
     */
    private Long furnitureId;

    /**
     * 家具名称
     */
    private String furnitureName;

    /**
     * 家具图标URL
     */
    private String furnitureIcon;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 状态
     */
    private int status;

    /**
     * 数量
     */
    private int quantity;

    /**
     * SKU规格描述
     */
    private String skuSpec;

    /**
     * 该订单项的合计金额（单价 × 数量）
     */
    private BigDecimal itemTotalPrice;

    public static OrderItemVO from(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        BeanUtil.copyProperties(item, vo);
        vo.setId(String.valueOf(item.getId()));
        vo.setOrderId(String.valueOf(item.getOrderId()));
        return vo;
    }
}
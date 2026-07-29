package gcy.system.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单项数据传输对象（DTO），用于承载创建或编辑订单项时的请求数据。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    /** 家具ID */
    private Long furnitureId;

    /** SKU ID */
    private Long skuId;

    /** 购买数量 */
    private Integer quantity;

}

package gcy.system.entity.vo;

import cn.hutool.core.bean.BeanUtil;
import gcy.system.entity.pojo.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemVO {

    private String id;

    private String orderId;

    private Long furnitureId;

    private String furnitureName;

    private String furnitureIcon;

    private BigDecimal price;

    private int status;

    private int quantity;

    private String skuSpec;

    private BigDecimal itemTotalPrice;

    public static OrderItemVO from(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        BeanUtil.copyProperties(item, vo);
        vo.setId(String.valueOf(item.getId()));
        vo.setOrderId(String.valueOf(item.getOrderId()));
        return vo;
    }
}
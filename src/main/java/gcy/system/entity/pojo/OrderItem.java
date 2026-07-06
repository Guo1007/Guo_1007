package gcy.system.entity.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.TableLogic;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {

    private Long id;

    private Long orderId;

    private Long furnitureId;

    private Long skuId;

    private String furnitureName;

    private String furnitureIcon;

    private BigDecimal price;

    private int quantity;

    private String skuSpec;

    private BigDecimal itemTotalPrice;

    @TableLogic
    private Integer deleted = 0;

}

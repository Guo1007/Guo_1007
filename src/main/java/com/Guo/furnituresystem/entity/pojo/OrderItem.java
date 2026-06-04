package com.Guo.furnituresystem.entity.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {

    private Long id;

    private Long orderId;

    private Long furnitureId;

    private String furnitureName;

    private String furnitureIcon;

    private BigDecimal price;

    private int quantity;

    private BigDecimal itemTotalPrice;

}

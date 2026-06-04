package com.example.furnituresystem.entity.vo;

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

    private BigDecimal itemTotalPrice;

}
package com.Guo.furnituresystem.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsVO {

    private long userCount;

    private long furnitureCount;

    private long orderCount;

    private BigDecimal totalAmount;
}

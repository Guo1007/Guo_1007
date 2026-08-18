package gcy.system.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 仪表盘统计数据视图对象，用于封装首页仪表盘展示的核心统计指标，
 * 包括用户总数、家具总数、订单总数及订单总金额等汇总数据。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsVO {

    /**
     * 用户总数
     */
    private long userCount;

    /**
     * 家具总数
     */
    private long furnitureCount;

    /**
     * 订单总数
     */
    private long orderCount;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
}

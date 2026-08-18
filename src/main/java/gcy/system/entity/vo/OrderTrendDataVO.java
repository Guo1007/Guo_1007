package gcy.system.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单趋势数据视图对象，用于承载订单按日期统计的趋势数据，
 * 通常用于前端图表（如折线图、柱状图）展示订单量随时间的变化情况。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrendDataVO {

    /**
     * 日期字符串，表示统计日期，格式通常为 yyyy-MM-dd
     */
    private String dateStr;

    /**
     * 该日期对应的订单数量
     */
    private Long count;
}

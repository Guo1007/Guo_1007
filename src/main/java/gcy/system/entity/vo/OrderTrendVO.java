package gcy.system.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单趋势视图对象，用于展示订单按日期维度的统计数据。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrendVO {

    /**
     * 统计日期
     */
    private String date;

    /**
     * 当日订单数量
     */
    private long count;
}

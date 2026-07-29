package gcy.system.service.admin;

import gcy.system.entity.dto.Result;

/**
 * 后台管理仪表盘服务接口
 * <p>
 * 提供仪表盘所需的核心统计数据，包括概览统计、订单趋势、
 * 低库存预警和热门家具排行等业务功能。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface IDashboardService {

    /**
     * 获取仪表盘概览统计数据
     * <p>
     * 查询并汇总系统的核心运营指标，如总订单数、总销售额、用户数量等，
     * 用于仪表盘首页概览展示。
     * </p>
     *
     * @return 包含概览统计数据的 {@link Result} 对象
     */
    Result getStats();

    /**
     * 获取订单趋势数据
     * <p>
     * 按时间段统计订单量的变化趋势，用于生成订单走势图，
     * 便于管理者了解订单量的周期变化规律。
     * </p>
     *
     * @return 包含订单趋势数据的 {@link Result} 对象
     */
    Result getOrderTrend();

    /**
     * 获取低库存预警列表
     * <p>
     * 查询当前库存量低于安全阈值的家具商品列表，
     * 提醒管理者及时补货，避免断货风险。
     * </p>
     *
     * @return 包含低库存商品列表的 {@link Result} 对象
     */
    Result getLowStock();

    /**
     * 获取热门家具排行榜
     * <p>
     * 根据销量或浏览次数统计当前最受欢迎的家具商品，
     * 用于热销排行展示和市场分析参考。
     * </p>
     *
     * @return 包含热门家具排行数据的 {@link Result} 对象
     */
    Result getTopFurniture();
}

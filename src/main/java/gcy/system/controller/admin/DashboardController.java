package gcy.system.controller.admin;

import gcy.system.entity.dto.Result;
import gcy.system.service.admin.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台仪表盘控制器
 * <p>
 * 提供仪表盘相关的数据查询接口，包括概览统计、订单趋势、低库存预警和热门家具排行。
 * 所有接口均返回统一的 {@link Result} 响应体。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Tag(name = "仪表盘", description = "仪表盘相关接口")
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    /**
     * 仪表盘服务接口，用于获取仪表盘各项数据
     */
    private final IDashboardService dashboardService;

    /**
     * 获取仪表盘概览统计数据
     * <p>
     * GET /admin/dashboard/stats
     * 返回包括订单数、销售额、用户数等关键指标的总览数据。
     * </p>
     *
     * @return 包含概览统计数据的 {@link Result} 对象
     */
    @Operation(summary = "获取仪表盘概览统计数据")
    @GetMapping("/stats")
    public Result stats() {
        return dashboardService.getStats();
    }

    /**
     * 获取订单趋势数据
     * <p>
     * GET /admin/dashboard/order-trend
     * 返回一段时间内订单量的变化趋势数据，通常用于绘制折线图或柱状图。
     * </p>
     *
     * @return 包含订单趋势数据的 {@link Result} 对象
     */
    @Operation(summary = "获取订单趋势数据")
    @GetMapping("/order-trend")
    public Result orderTrend() {
        return dashboardService.getOrderTrend();
    }

    /**
     * 获取低库存预警数据
     * <p>
     * GET /admin/dashboard/low-stock
     * 返回当前库存低于预警线的家具列表，帮助管理员及时补货。
     * </p>
     *
     * @return 包含低库存预警信息的 {@link Result} 对象
     */
    @Operation(summary = "获取低库存预警数据")
    @GetMapping("/low-stock")
    public Result lowStock() {
        return dashboardService.getLowStock();
    }

    /**
     * 获取热门家具排行数据
     * <p>
     * GET /admin/dashboard/top-furniture
     * 返回按销量或热度排序的家具排行列表。
     * </p>
     *
     * @return 包含热门家具排行数据的 {@link Result} 对象
     */
    @Operation(summary = "获取热门家具排行数据")
    @GetMapping("/top-furniture")
    public Result topFurniture() {
        return dashboardService.getTopFurniture();
    }
}

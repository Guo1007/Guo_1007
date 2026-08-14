package gcy.system.service.admin.Impl;

import gcy.system.entity.dto.Result;
import gcy.system.entity.vo.*;
import gcy.system.mapper.FurnitureMapper;
import gcy.system.mapper.OrderItemMapper;
import gcy.system.mapper.OrderMapper;
import gcy.system.mapper.UserMapper;
import gcy.system.service.admin.IDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仪表盘服务实现类
 * <p>
 * 负责提供管理后台仪表盘所需的核心统计数据，包括：
 * 用户/家具/订单总量、总营收、近7日订单趋势、低库存预警、热销家具排行。
 * 各方法通过调用对应的 Mapper 层查询数据库，并将结果封装为统一响应 {@link Result} 返回。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final UserMapper userMapper;

    private final FurnitureMapper furnitureMapper;

    private final OrderMapper orderMapper;

    private final OrderItemMapper orderItemMapper;

    /**
     * 获取仪表盘核心统计数据
     * <p>
     * 依次查询用户总数、家具总数、订单总数以及总营收金额，
     * 封装为 {@link DashboardStatsVO} 后通过 {@link Result#ok(Object)} 返回。
     * </p>
     *
     * @return 包含用户数、家具数、订单数和总营收的统计结果
     */
    @Override
    public Result getStats() {
        long userCount = userMapper.selectCount(null);
        long furnitureCount = furnitureMapper.selectCount(null);
        long orderCount = orderMapper.selectCount(null);
        BigDecimal totalAmount = orderMapper.selectTotalRevenue();

        DashboardStatsVO vo = new DashboardStatsVO(userCount, furnitureCount, orderCount, totalAmount);
        return Result.ok(vo);
    }

    /**
     * 获取近7日订单趋势数据
     * <p>
     * 查询从6天前至今（共7天）的每日订单数量，将数据库返回的日期-数量映射为连续的7天数据，
     * 缺失的日期补0，确保前端折线图展示完整且日期连续。
     * </p>
     *
     * @return 按日期倒序排列的近7日订单趋势列表
     */
    @Override
    public Result getOrderTrend() {
        LocalDateTime since = LocalDate.now().minusDays(6).atStartOfDay();
        List<OrderTrendDataVO> raw = orderMapper.selectOrderTrend(since);

        Map<String, Long> dateMap = raw.stream()
                .collect(Collectors.toMap(
                        OrderTrendDataVO::getDateStr,
                        OrderTrendDataVO::getCount
                ));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<OrderTrendVO> trendList = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String dateStr = LocalDate.now().minusDays(i).format(fmt);
            trendList.add(new OrderTrendVO(dateStr, dateMap.getOrDefault(dateStr, 0L)));
        }

        return Result.ok(trendList);
    }

    /**
     * 获取低库存家具列表
     * <p>
     * 调用家具 Mapper 查询当前库存量低于预警阈值的家具记录，
     * 直接返回查询结果，由前端展示低库存预警信息。
     * </p>
     *
     * @return 低库存家具列表，每条记录包含家具基本信息及当前库存量
     */
    @Override
    public Result getLowStock() {
        List<LowStockVO> list = furnitureMapper.selectLowStock();
        return Result.ok(list);
    }

    /**
     * 获取热销家具排行
     * <p>
     * 调用订单明细 Mapper 统计各家具的销售数量并降序排列，
     * 返回销量最高的前几名家具信息，用于仪表盘热销排行展示。
     * </p>
     *
     * @return 按销量降序排列的热销家具列表
     */
    @Override
    public Result getTopFurniture() {
        List<TopFurnitureVO> list = orderItemMapper.selectTopFurniture();
        return Result.ok(list);
    }
}

package gcy.system.service.admin.Impl;

import gcy.system.entity.dto.Result;
import gcy.system.entity.vo.DashboardStatsVO;
import gcy.system.entity.vo.LowStockVO;
import gcy.system.entity.vo.OrderTrendDataVO;
import gcy.system.entity.vo.OrderTrendVO;
import gcy.system.entity.vo.TopFurnitureVO;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final UserMapper userMapper;

    private final FurnitureMapper furnitureMapper;

    private final OrderMapper orderMapper;

    private final OrderItemMapper orderItemMapper;

    @Override
    public Result getStats() {
        long userCount = userMapper.selectCount(null);
        long furnitureCount = furnitureMapper.selectCount(null);
        long orderCount = orderMapper.selectCount(null);
        BigDecimal totalAmount = orderMapper.selectTotalRevenue();

        DashboardStatsVO vo = new DashboardStatsVO(userCount, furnitureCount, orderCount, totalAmount);
        return Result.ok(vo);
    }

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

    @Override
    public Result getLowStock() {
        List<LowStockVO> list = furnitureMapper.selectLowStock();
        return Result.ok(list);
    }

    @Override
    public Result getTopFurniture() {
        List<TopFurnitureVO> list = orderItemMapper.selectTopFurniture();
        return Result.ok(list);
    }
}

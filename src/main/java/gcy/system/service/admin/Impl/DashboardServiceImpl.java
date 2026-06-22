package gcy.system.service.admin.Impl;

import gcy.system.entity.dto.Result;
import gcy.system.entity.vo.DashboardStatsVO;
import gcy.system.entity.vo.OrderTrendVO;
import gcy.system.entity.vo.TopFurnitureVO;
import gcy.system.mapper.FurnitureMapper;
import gcy.system.mapper.OrderItemMapper;
import gcy.system.mapper.OrderMapper;
import gcy.system.mapper.UserMapper;
import gcy.system.service.admin.IDashboardService;
import jakarta.annotation.Resource;
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
public class DashboardServiceImpl implements IDashboardService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private FurnitureMapper furnitureMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderItemMapper orderItemMapper;

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
        List<Map<String, Object>> raw = orderMapper.selectOrderTrend(since);

        Map<String, Long> dateMap = raw.stream()
                .collect(Collectors.toMap(
                        m -> m.get("date_str").toString(),
                        m -> ((Number) m.get("count")).longValue()
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
        List<Map<String, Object>> list = furnitureMapper.selectLowStock();
        return Result.ok(list);
    }

    @Override
    public Result getTopFurniture() {
        List<Map<String, Object>> raw = orderItemMapper.selectTopFurniture();

        List<TopFurnitureVO> list = raw.stream().map(m -> {
            Long furnitureId = ((Number) m.get("furniture_id")).longValue();
            String furnitureName = (String) m.get("furniture_name");
            String furnitureIcon = (String) m.get("furniture_icon");
            long totalSold = ((Number) m.get("total_sold")).longValue();
            return new TopFurnitureVO(furnitureId, furnitureName, furnitureIcon, totalSold);
        }).toList();

        return Result.ok(list);
    }
}

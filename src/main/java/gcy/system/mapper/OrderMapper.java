package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.Order;
import gcy.system.entity.vo.OrderTrendDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT DATE(create_time) AS date_str, COUNT(*) AS count FROM `order` " +
            "WHERE create_time >= #{since} GROUP BY DATE(create_time) ORDER BY date_str")
    List<OrderTrendDataVO> selectOrderTrend(@Param("since") LocalDateTime since);

    @Select("SELECT COALESCE(SUM(total_price), 0) FROM `order` WHERE status IN (1, 2, 3)")
    BigDecimal selectTotalRevenue();
}

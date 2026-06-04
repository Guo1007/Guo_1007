package com.Guo.furnituresystem.mapper;

import com.Guo.furnituresystem.entity.pojo.OrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    @Select("SELECT oi.furniture_id, f.f_name AS furniture_name, f.f_icon AS furniture_icon, SUM(oi.quantity) AS total_sold " +
            "FROM order_item oi " +
            "INNER JOIN `order` o ON oi.order_id = o.id " +
            "INNER JOIN furniture f ON oi.furniture_id = f.id " +
            "WHERE o.status IN (1, 2, 3) " +
            "GROUP BY oi.furniture_id, f.f_name, f.f_icon " +
            "ORDER BY total_sold DESC LIMIT 5")
    List<Map<String, Object>> selectTopFurniture();
}

package com.example.furnituresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.furnituresystem.entity.pojo.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    @Select("SELECT oi.furniture_id, oi.furniture_name, oi.furniture_icon, SUM(oi.quantity) AS total_sold " +
            "FROM order_item oi INNER JOIN `order` o ON oi.order_id = o.id " +
            "WHERE o.status IN (1, 2, 3) " +
            "GROUP BY oi.furniture_id, oi.furniture_name, oi.furniture_icon " +
            "ORDER BY total_sold DESC LIMIT 5")
    List<Map<String, Object>> selectTopFurniture();
}

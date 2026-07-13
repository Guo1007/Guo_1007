package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.OrderItem;
import gcy.system.entity.vo.TopFurnitureVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    @Select("SELECT oi.furniture_id AS furnitureId, f.f_name AS furnitureName, f.f_icon AS furnitureIcon, SUM(oi.quantity) AS totalSold " +
            "FROM order_item oi " +
            "INNER JOIN `order` o ON oi.order_id = o.id " +
            "INNER JOIN furniture f ON oi.furniture_id = f.id " +
            "WHERE o.status IN (1, 2, 3, 5) " +
            "AND oi.deleted = 0 AND o.deleted = 0 AND f.deleted = 0 " +
            "GROUP BY oi.furniture_id, f.f_name, f.f_icon " +
            "ORDER BY totalSold DESC LIMIT 5")
    List<TopFurnitureVO> selectTopFurniture();

    @Select("SELECT oi.furniture_id AS furnitureId, f.f_name AS furnitureName, f.f_icon AS furnitureIcon, SUM(oi.quantity) AS totalSold " +
            "FROM order_item oi " +
            "INNER JOIN `order` o ON oi.order_id = o.id " +
            "INNER JOIN furniture f ON oi.furniture_id = f.id " +
            "WHERE o.status IN (1, 2, 3, 5) " +
            "AND oi.deleted = 0 AND o.deleted = 0 AND f.deleted = 0 " +
            "GROUP BY oi.furniture_id, f.f_name, f.f_icon " +
            "ORDER BY totalSold DESC LIMIT #{limit}")
    List<TopFurnitureVO> selectTopSelling(@Param("limit") int limit);
}

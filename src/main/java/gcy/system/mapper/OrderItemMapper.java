package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.OrderItem;
import gcy.system.entity.vo.TopFurnitureVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单项 Mapper 接口，提供订单项相关的数据库查询操作。
 * 继承 MyBatis-Plus 的 BaseMapper，自动拥有基本的 CRUD 方法。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 查询销量排名前5的家具。
     * 通过关联订单项表(order_item)、订单表(order)和家具表(furniture)，
     * 统计已支付/已发货/已完成等有效状态订单中各家具的总销量，
     * 按销量降序排列并取前5条记录。
     *
     * @return 销量排名前5的家具列表，每个元素包含家具ID、名称、图标和总销量
     */
    @Select("SELECT oi.furniture_id AS furnitureId, f.f_name AS furnitureName, f.f_icon AS furnitureIcon, SUM(oi.quantity) AS totalSold " +
            "FROM order_item oi " +
            "INNER JOIN `order` o ON oi.order_id = o.id " +
            "INNER JOIN furniture f ON oi.furniture_id = f.id " +
            "WHERE o.status IN (1, 2, 3, 5) " +
            "AND oi.deleted = 0 AND o.deleted = 0 AND f.deleted = 0 " +
            "GROUP BY oi.furniture_id, f.f_name, f.f_icon " +
            "ORDER BY totalSold DESC LIMIT 5")
    List<TopFurnitureVO> selectTopFurniture();

    /**
     * 查询销量排名前N的家具（热门家具排行）。
     * 通过关联订单项表(order_item)、订单表(order)和家具表(furniture)，
     * 统计已支付/已发货/已完成等有效状态订单中各家具的总销量，
     * 按销量降序排列并取指定数量的记录。
     *
     * @param limit 返回的家具数量上限
     * @return 销量排名前N的家具列表，每个元素包含家具ID、名称、图标和总销量
     */
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

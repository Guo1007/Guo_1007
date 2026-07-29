package gcy.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.OrderItem;

/**
 * 订单明细服务接口，提供订单明细相关的业务查询功能。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface IOrderItemService extends IService<OrderItem> {

    /**
     * 根据订单ID获取订单的详细信息，包括订单基本信息和关联的订单明细列表。
     *
     * @param orderId 订单ID，用于查询该订单下的所有明细项
     * @return 包含订单详情数据的通用结果对象
     */
    Result getOrderDetail(Long orderId);

}

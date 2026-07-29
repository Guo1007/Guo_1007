package gcy.system.service;


import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.CartFormDTO;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.Order;


/**
 * 订单服务接口，定义订单相关的业务操作，包括创建订单、查询订单、支付、取消、确认收货及删除等功能。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface IOrderService extends IService<Order> {

    /**
     * 根据购物车表单数据创建订单。
     *
     * @param dto 购物车表单数据传输对象，包含用户选购的商品信息
     * @return 包含创建结果的操作结果对象
     */
    Result createOrder(CartFormDTO dto);

    /**
     * 根据用户ID分页查询该用户的订单列表。
     *
     * @param current 当前页码
     * @param size    每页显示的订单数量
     * @return 包含分页订单数据的操作结果对象
     */
    Result getOrderByUserId(Long current, Long size);

    /**
     * 根据订单ID执行支付操作。
     *
     * @param id 待支付的订单ID
     * @return 包含支付结果的操作结果对象
     */
    Result payOrderById(Long id);

    /**
     * 根据订单ID取消指定的订单。
     *
     * @param id 待取消的订单ID
     * @return 包含取消结果的操作结果对象
     */
    Result cancelOrder(Long id);

    /**
     * 根据订单ID取消已超时的订单。
     *
     * @param id 超时未支付的订单ID
     * @return 包含取消结果的操作结果对象
     */
    Result cancelTimeoutOrder(Long id);

    /**
     * 根据订单ID确认收货。
     *
     * @param id 待确认收货的订单ID
     * @return 包含确认收货结果的操作结果对象
     */
    Result confirmReceipt(Long id);

    /**
     * 根据订单ID删除当前用户的订单记录。
     *
     * @param id 待删除的订单ID
     * @return 包含删除结果的操作结果对象
     */
    Result deleteMyOrder(Long id);

}

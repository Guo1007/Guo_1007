package gcy.system.service;


import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.CartFormDTO;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.Order;


public interface IOrderService extends IService<Order> {

    Result createOrder(CartFormDTO dto);

    Result getOrderByUserId(Long current, Long size);

    Result payOrderById(Long id);

    Result cancelOrder(Long id);

    /**
     * 系统自动取消超时未支付订单
     */
    Result cancelTimeoutOrder(Long id);

    Result confirmReceipt(Long id);

}

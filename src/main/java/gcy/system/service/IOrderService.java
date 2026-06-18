package gcy.system.service;


import gcy.system.entity.dto.CartFormDTO;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.Order;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IOrderService extends IService<Order> {

    Result createOrder(CartFormDTO dto);

    Result getOrderByUserId(Long current, Long size);

    Result payOrderById(Long id);

    Result cancelOrder(Long id);

    Result confirmReceipt(Long id);

}

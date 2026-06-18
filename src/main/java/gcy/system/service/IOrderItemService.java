package gcy.system.service;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.OrderItem;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IOrderItemService extends IService<OrderItem> {

    Result getOrderDetail(Long orderId);

}

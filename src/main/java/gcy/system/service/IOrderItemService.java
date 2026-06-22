package gcy.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.OrderItem;

public interface IOrderItemService extends IService<OrderItem> {

    Result getOrderDetail(Long orderId);

}

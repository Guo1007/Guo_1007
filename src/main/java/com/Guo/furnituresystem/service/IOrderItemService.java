package com.Guo.furnituresystem.service;

import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.entity.pojo.OrderItem;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IOrderItemService extends IService<OrderItem> {

    Result getOrderDetail(Long orderId);

}

package com.example.furnituresystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.pojo.OrderItem;

public interface IOrderItemService extends IService<OrderItem> {

    Result getOrderDetail(Long orderId);

}

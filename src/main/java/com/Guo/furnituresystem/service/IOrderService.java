package com.Guo.furnituresystem.service;


import com.Guo.furnituresystem.entity.dto.CartFormDTO;
import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.entity.pojo.Order;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IOrderService extends IService<Order> {

    Result createOrder(CartFormDTO dto);

    Result getOrderByUserId(Long current, Long size);

    Result payOrderById(Long id);

    Result cancelOrder(Long id);

    Result confirmReceipt(Long id);

}

package com.example.furnituresystem.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.furnituresystem.entity.dto.CartFormDTO;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.pojo.Order;


public interface IOrderService extends IService<Order> {

    Result createOrder(CartFormDTO dto);

    Result getOrderByUserId(Long current, Long size);

    Result payOrderById(Long id);

    Result cancelOrder(Long id);

    Result confirmReceipt(Long id);

}

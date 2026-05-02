package com.example.furnituresystem.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.pojo.Order;

public interface IOrderManageService extends IService<Order> {

    Result getOrderList(Integer current, Integer size, Integer userId,
                        Integer status, String phone, String consignee);

    Result shipOrderById(Long id);

}

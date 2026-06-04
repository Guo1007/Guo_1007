package com.Guo.furnituresystem.service.admin;

import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.entity.pojo.Order;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IOrderManageService extends IService<Order> {

    Result getOrderList(Integer current, Integer size, Integer userId,
                        Integer status, String phone, String consignee);

    Result shipOrderById(Long id);

}

package com.example.furnituresystem.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.pojo.Order;
import com.example.furnituresystem.entity.pojo.OrderItem;
import com.example.furnituresystem.entity.vo.OrderItemVO;
import com.example.furnituresystem.entity.vo.OrderVO;
import com.example.furnituresystem.mapper.OrderItemMapper;
import com.example.furnituresystem.mapper.OrderMapper;
import com.example.furnituresystem.service.IOrderItemService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class IOrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements IOrderItemService {

    @Resource
    private OrderMapper orderMapper;

    @Override
    public Result getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        OrderVO vo = new OrderVO();
        BeanUtil.copyProperties(order, vo);
        vo.setId(String.valueOf(order.getId()));
        List<OrderItem> items = query().eq("order_id", orderId).list();
        List<OrderItemVO> itemVOList = new ArrayList<>();
        for (OrderItem item : items) {
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtil.copyProperties(item, itemVO);
            itemVO.setId(String.valueOf(item.getId()));
            itemVO.setOrderId(String.valueOf(orderId));
            itemVOList.add(itemVO);
        }
        vo.setItemList(itemVOList);
        return Result.ok(vo);
    }


}

package com.Guo.furnituresystem.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.entity.pojo.Order;
import com.Guo.furnituresystem.entity.pojo.OrderItem;
import com.Guo.furnituresystem.entity.vo.OrderItemVO;
import com.Guo.furnituresystem.entity.vo.OrderVO;
import com.Guo.furnituresystem.mapper.OrderItemMapper;
import com.Guo.furnituresystem.mapper.OrderMapper;
import com.Guo.furnituresystem.service.IOrderItemService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements IOrderItemService {

    @Resource
    private OrderMapper orderMapper;

    @Override
    public Result getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        List<OrderItem> items = list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        OrderVO vo = new OrderVO();
        BeanUtil.copyProperties(order, vo);
        vo.setId(String.valueOf(order.getId()));
        vo.setItemList(items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtil.copyProperties(item, itemVO);
            itemVO.setId(String.valueOf(item.getId()));
            itemVO.setOrderId(String.valueOf(orderId));
            return itemVO;
        }).collect(Collectors.toList()));
        return Result.ok(vo);
    }

}

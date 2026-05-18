package com.example.furnituresystem.service.admin.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.pojo.Order;
import com.example.furnituresystem.entity.pojo.OrderItem;
import com.example.furnituresystem.entity.vo.OrderItemVO;
import com.example.furnituresystem.entity.vo.OrderVO;
import com.example.furnituresystem.exception.BusinessException;
import com.example.furnituresystem.mapper.admin.OrderManageMapper;
import com.example.furnituresystem.service.IOrderItemService;
import com.example.furnituresystem.service.admin.IOrderManageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
public class IOrderManageServiceImpl extends ServiceImpl<OrderManageMapper, Order>
        implements IOrderManageService {

    @Resource
    private OrderManageMapper orderManageMapper;

    @Resource
    private IOrderItemService orderItemService;

    @Override
    public Result getOrderList(Integer current, Integer size, Integer userId,
                               Integer status, String phone, String consignee) {
        Page<Order> page = new Page<>(current, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        if (StrUtil.isNotBlank(phone)) {
            wrapper.like(Order::getPhone, phone);
        }
        if (StrUtil.isNotBlank(consignee)) {
            wrapper.like(Order::getConsignee, consignee);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> resultPage = orderManageMapper.selectPage(page, wrapper);
        List<OrderVO> voList = new ArrayList<>();
        for (Order order : resultPage.getRecords()) {
            OrderVO vo = new OrderVO();
            BeanUtil.copyProperties(order, vo);
            vo.setId(String.valueOf(order.getId()));
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, order.getId());
            List<OrderItem> items = orderItemService.list(itemWrapper);
            if (items == null) {
                items = Collections.emptyList();
            }
            List<OrderItemVO> itemVOList = new ArrayList<>();
            for (OrderItem item : items) {
                OrderItemVO itemVO = new OrderItemVO();
                BeanUtil.copyProperties(item, itemVO);
                itemVO.setId(String.valueOf(item.getId()));
                itemVO.setOrderId(String.valueOf(item.getOrderId()));
                itemVOList.add(itemVO);
            }
            vo.setItemList(itemVOList);
            voList.add(vo);
        }
        Page<OrderVO> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setTotal(resultPage.getTotal());
        voPage.setSize(resultPage.getSize());
        voPage.setCurrent(resultPage.getCurrent());
        voPage.setPages(resultPage.getPages());
        return Result.ok(voPage);
    }

    @Override
    @Transactional
    public Result shipOrderById(Long id) {
        Order order = getById(id);
        if (order == null) {
            return Result.fail("订单不存在！");
        }
        if (order.getStatus() != 1) {
            if (order.getStatus() == 0) {
                return Result.fail("该订单还未支付！");
            } else if (order.getStatus() == 2 || order.getStatus() == 3) {
                return Result.fail("该订单已经发货了，无需重复操作！");
            } else {
                return Result.fail("订单已被取消！");
            }
        }
        order.setStatus(2);
        order.setShipTime(LocalDateTime.now());
        boolean success = updateById(order);
        if (!success) {
            throw new BusinessException("发货失败，请联系系统管理人员检查！");
        }
        return Result.ok();
    }


}

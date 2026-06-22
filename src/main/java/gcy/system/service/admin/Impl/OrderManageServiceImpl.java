package gcy.system.service.admin.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.RocketMQMessage;
import gcy.system.entity.pojo.Order;
import gcy.system.entity.pojo.OrderItem;
import gcy.system.entity.pojo.User;
import gcy.system.entity.vo.OrderItemVO;
import gcy.system.entity.vo.OrderVO;
import gcy.system.exception.BusinessException;
import gcy.system.mapper.UserMapper;
import gcy.system.mapper.admin.OrderManageMapper;
import gcy.system.service.EmailService;
import gcy.system.service.IOrderItemService;
import gcy.system.service.admin.IOrderManageService;
import gcy.system.utils.JvmLockManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static gcy.system.utils.OrderStatus.*;
import static gcy.system.utils.RedisConstants.ORDER_SHIP_KEY;

@Slf4j
@Service
public class OrderManageServiceImpl extends ServiceImpl<OrderManageMapper, Order>
        implements IOrderManageService {

    @Resource
    private OrderManageMapper orderManageMapper;

    @Resource
    private IOrderItemService orderItemService;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private EmailService emailService;

    @Resource
    private UserMapper userMapper;

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
        List<Order> orders = resultPage.getRecords();
        Map<Long, List<OrderItem>> itemMap = new HashMap<>();
        if (!orders.isEmpty()) {
            List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
            List<OrderItem> allItems = orderItemService.list(
                    new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
            itemMap.putAll(allItems.stream().collect(Collectors.groupingBy(OrderItem::getOrderId)));
        }
        List<OrderVO> voList = orders.stream()
                .map(order -> toOrderVO(order, itemMap.getOrDefault(order.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
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
        String lockKey = ORDER_SHIP_KEY + id;
        ReentrantLock lock = JvmLockManager.getLock(lockKey);
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                Order order = getById(id);
                if (order == null) {
                    return Result.fail("订单不存在！");
                }
                int status = order.getStatus();
                if (status != PAID.getCode()) {
                    if (status == PENDING_PAYMENT.getCode()) {
                        return Result.fail("该订单还未支付！");
                    } else if (status == SHIPPED.getCode() || status == COMPLETED.getCode() || status == REVIEWED.getCode()) {
                        return Result.ok();
                    } else {
                        return Result.fail("订单已被取消！");
                    }
                }
                boolean success = update()
                        .set("status", SHIPPED.getCode())
                        .set("ship_time", LocalDateTime.now())
                        .eq("id", id)
                        .eq("status", PAID.getCode())
                        .update();
                if (!success) {
                    Order updated = getById(id);
                    if (updated.getStatus() == SHIPPED.getCode() || updated.getStatus() == COMPLETED.getCode() || updated.getStatus() == REVIEWED.getCode()) {
                        return Result.ok();
                    }
                    throw new BusinessException("发货失败，请联系系统管理人员检查！");
                }
                sendShipMq(order);
                return Result.ok();
            } else {
                return Result.fail("发货处理中，请勿重复操作");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("订单发货获取锁被中断: orderId={}", id, e);
            return Result.fail("系统繁忙，请稍后重试");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void sendShipMq(Order order) {
        User user = userMapper.selectById(order.getUserId());
        if (user == null || StrUtil.isBlank(user.getEmail())) {
            return;
        }
        try {
            RocketMQMessage msg = new RocketMQMessage();
            msg.setType("order-shipped");
            msg.setOrderId(order.getId());
            msg.setUserId(order.getUserId());
            msg.setUserEmail(user.getEmail());
            msg.setUserName(user.getUserName());
            msg.setTitle("订单已发货");
            msg.setContent("您的订单 #" + order.getId() + " 已发货，请留意收货。");
            msg.setStatusIcon("🚚");
            msg.setStatusColor("#3498db");
            rocketMQTemplate.convertAndSend("order-status-topic", JSONUtil.toJsonStr(msg));
            log.info("发货MQ消息已发送: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("MQ发送失败，回退到直接邮件发送: orderId={}", order.getId(), e);
            emailService.sendOrderStatusEmail(user.getEmail(), order.getId(), "订单已发货",
                    "您的订单 #" + order.getId() + " 已发货，请留意收货。",
                    "🚚", "#3498db", order.getTotalPrice().toString(), user.getUserName());
        }
    }

    private OrderVO toOrderVO(Order order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        BeanUtil.copyProperties(order, vo);
        vo.setId(String.valueOf(order.getId()));
        vo.setItemList(items.stream().map(this::toOrderItemVO).collect(Collectors.toList()));
        return vo;
    }

    private OrderItemVO toOrderItemVO(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        BeanUtil.copyProperties(item, vo);
        vo.setId(String.valueOf(item.getId()));
        vo.setOrderId(String.valueOf(item.getOrderId()));
        return vo;
    }

    private static final DateTimeFormatter CSV_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void exportOrders(PrintWriter w) throws IOException {
        List<Order> orders = orderManageMapper.selectList(
                new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime));

        w.println("﻿订单号,用户ID,收货人,电话,地址,金额,状态,备注,创建时间,支付时间,发货时间");
        String[] statusMap = {"待支付", "已支付", "已发货", "已完成", "已取消", "已评价"};

        for (Order o : orders) {
            String statusText = o.getStatus() >= 0 && o.getStatus() < statusMap.length
                    ? statusMap[o.getStatus()] : "未知";
            w.printf("%d,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                    o.getId(),
                    o.getUserId(),
                    csvEscape(o.getConsignee()),
                    csvEscape(o.getPhone()),
                    csvEscape(o.getAddress()),
                    o.getTotalPrice(),
                    statusText,
                    csvEscape(o.getRemark()),
                    csvDate(o.getCreateTime()),
                    csvDate(o.getPayTime()),
                    csvDate(o.getShipTime()));
        }
        w.flush();
    }

    private String csvEscape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    private String csvDate(LocalDateTime dt) {
        if (dt == null) return "";
        return "\t" + dt.format(CSV_DATE_FMT);
    }

}

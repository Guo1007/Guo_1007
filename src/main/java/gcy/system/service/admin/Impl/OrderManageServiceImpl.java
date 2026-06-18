package gcy.system.service.admin.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static gcy.system.utils.RedisConstants.ORDER_SHIP_KEY;

@Slf4j
@Service
public class OrderManageServiceImpl extends ServiceImpl<OrderManageMapper, Order>
        implements IOrderManageService {

    // 订单状态常量
    private static final int ORDER_STATUS_PENDING_PAYMENT = 0;  // 待支付
    private static final int ORDER_STATUS_PAID = 1;             // 已支付
    private static final int ORDER_STATUS_SHIPPED = 2;          // 已发货
    private static final int ORDER_STATUS_COMPLETED = 3;        // 已完成
    private static final int ORDER_STATUS_CANCELLED = 4;        // 已取消
    private static final int ORDER_STATUS_REVIEWED = 5;         // 已评价

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

    @Resource
    private RedissonClient redissonClient;

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
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                Order order = getById(id);
                if (order == null) {
                    return Result.fail("订单不存在！");
                }
                int status = order.getStatus();
                if (status != ORDER_STATUS_PAID) {
                    if (status == ORDER_STATUS_PENDING_PAYMENT) {
                        return Result.fail("该订单还未支付！");
                    } else if (status == ORDER_STATUS_SHIPPED || status == ORDER_STATUS_COMPLETED || status == ORDER_STATUS_REVIEWED) {
                        return Result.ok();
                    } else {
                        return Result.fail("订单已被取消！");
                    }
                }
                boolean success = update()
                        .set("status", ORDER_STATUS_SHIPPED)
                        .set("ship_time", LocalDateTime.now())
                        .eq("id", id)
                        .eq("status", ORDER_STATUS_PAID)
                        .update();
                if (!success) {
                    Order updated = getById(id);
                    if (updated.getStatus() == ORDER_STATUS_SHIPPED || updated.getStatus() == ORDER_STATUS_COMPLETED || updated.getStatus() == ORDER_STATUS_REVIEWED) {
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

}

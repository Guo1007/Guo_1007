package com.Guo.furnituresystem.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.Guo.furnituresystem.entity.dto.*;
import com.Guo.furnituresystem.entity.pojo.Furniture;
import com.Guo.furnituresystem.entity.pojo.Order;
import com.Guo.furnituresystem.entity.pojo.OrderItem;
import com.Guo.furnituresystem.entity.pojo.User;
import com.Guo.furnituresystem.entity.vo.OrderItemVO;
import com.Guo.furnituresystem.entity.vo.OrderVO;
import com.Guo.furnituresystem.exception.BusinessException;
import com.Guo.furnituresystem.mapper.FurnitureMapper;
import com.Guo.furnituresystem.mapper.OrderMapper;
import com.Guo.furnituresystem.mapper.UserMapper;
import com.Guo.furnituresystem.service.EmailService;
import com.Guo.furnituresystem.service.IOrderItemService;
import com.Guo.furnituresystem.service.IOrderService;
import com.Guo.furnituresystem.utils.RedisData;
import com.Guo.furnituresystem.utils.UserHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.Guo.furnituresystem.utils.RedisConstants.*;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private static final int ORDER_STATUS_PENDING_PAYMENT = 0;  // 待支付
    private static final int ORDER_STATUS_PAID = 1;             // 已支付
    private static final int ORDER_STATUS_SHIPPED = 2;          // 已发货
    private static final int ORDER_STATUS_COMPLETED = 3;        // 已完成
    private static final int ORDER_STATUS_CANCELLED = 4;        // 已取消
    private static final int ORDER_STATUS_REVIEWED = 5;         // 已评价

    @Resource
    private FurnitureMapper furnitureMapper;

    @Resource
    private IOrderItemService orderItemService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private EmailService emailService;

    @Resource
    private UserMapper userMapper;

    /**
     * 执行带分布式锁的操作
     *
     * @param lockKey   锁的key
     * @param waitTime  等待时间（秒）
     * @param leaseTime 租赁时间（秒），-1表示使用看门狗自动续期
     * @param action    要执行的操作
     * @return 操作结果
     */
    private Result executeWithLock(String lockKey, long waitTime, long leaseTime, java.util.function.Supplier<Result> action) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean locked;
            if (leaseTime > 0) {
                locked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            } else {
                locked = lock.tryLock(waitTime, TimeUnit.SECONDS);
            }

            if (locked) {
                return action.get();
            } else {
                return Result.fail("操作处理中，请勿重复提交");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: lockKey={}", lockKey, e);
            return Result.fail("系统繁忙，请稍后重试");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional
    public Result createOrder(CartFormDTO dto) {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();
        String lockKey = ORDER_CREATE_KEY + userId;
        return executeWithLock(lockKey, 5, -1, () -> {
            if (StrUtil.isBlank(dto.getConsignee()) || StrUtil.isBlank(dto.getAddress()) || StrUtil.isBlank(dto.getPhone())) {
                return Result.fail("请填写完整的收货信息");
            }
            List<OrderItemDTO> items = dto.getItemList();
            if (items == null || items.isEmpty()) {
                return Result.fail("购物车为空");
            }
            Order order = BeanUtil.copyProperties(dto, Order.class);
            order.setCreateTime(LocalDateTime.now());
            order.setStatus(ORDER_STATUS_PENDING_PAYMENT);
            order.setUserId(userId);
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();
            for (OrderItemDTO itemDto : items) {
                Long furnitureId = itemDto.getFurnitureId();
                int quantity = itemDto.getQuantity();
                Furniture furniture = furnitureMapper.selectById(furnitureId);
                if (furniture == null) {
                    throw new BusinessException("商品不存在或已下架");
                }
                if (furniture.getStock() < quantity) {
                    throw new BusinessException("商品 " + furniture.getFName() + " 库存不足，当前库存: " + furniture.getStock());
                }
                int rows = furnitureMapper.decrementStock(furnitureId, quantity);
                if (rows == 0) {
                    throw new BusinessException("商品 " + furniture.getFName() + " 库存发生变化，请重新下单");
                }
                furniture.setStock(furniture.getStock() - quantity);
                updateFurnitureCache(furniture);
                BigDecimal itemTotal = furniture.getPrice().multiply(new BigDecimal(quantity));
                totalAmount = totalAmount.add(itemTotal);
                OrderItem orderItem = new OrderItem();
                orderItem.setFurnitureId(furnitureId);
                orderItem.setPrice(furniture.getPrice());
                orderItem.setQuantity(quantity);
                orderItem.setItemTotalPrice(itemTotal);
                orderItem.setFurnitureName(furniture.getFName());
                orderItem.setFurnitureIcon(furniture.getFIcon());
                orderItems.add(orderItem);
            }
            order.setTotalPrice(totalAmount);
            save(order);
            Long orderId = order.getId();
            for (OrderItem item : orderItems) {
                item.setOrderId(orderId);
            }
            boolean success = orderItemService.saveBatch(orderItems);
            if (!success) {
                throw new BusinessException("订单明细保存失败");
            }
            return Result.ok(orderId);
        });
    }

    @Override
    public Result getOrderByUserId(Long current, Long size) {
        Page<Order> page = new Page<>(current != null ? current : 1L, size != null ? size : 10L);
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> resultPage = this.page(page, wrapper);
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
    public Result payOrderById(Long id) {
        String lockKey = ORDER_PAY_KEY + id;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                Order order = getById(id);
                if (order == null) {
                    return Result.fail("订单不存在！");
                }
                Long userId = UserHolder.getUser().getId();
                int status = order.getStatus();
                if (!order.getUserId().equals(userId)) {
                    return Result.fail("无权支付该订单！");
                }
                if (status != ORDER_STATUS_PENDING_PAYMENT) {
                    if (status == ORDER_STATUS_PAID || status == ORDER_STATUS_SHIPPED) {
                        return Result.ok();
                    }
                    return Result.fail("订单状态异常，请稍后重新支付或取消订单！");
                }
                boolean success = update()
                        .set("status", ORDER_STATUS_PAID)
                        .set("pay_time", LocalDateTime.now())
                        .eq("id", id)
                        .eq("status", ORDER_STATUS_PENDING_PAYMENT)
                        .update();
                if (!success) {
                    Order updated = getById(id);
                    if (updated.getStatus() == ORDER_STATUS_PAID || updated.getStatus() == ORDER_STATUS_SHIPPED) {
                        return Result.ok();
                    }
                    return Result.fail("支付失败，请重试");
                }
                sendOrderStatusMq(order, "order-paid", "订单支付成功",
                        "您的订单 #" + id + " 已支付成功，我们将尽快为您发货。订单金额：¥" + order.getTotalPrice());
                return Result.ok();
            } else {
                return Result.fail("支付处理中，请勿重复点击");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("支付订单获取锁被中断: orderId={}", id, e);
            return Result.fail("系统繁忙，请稍后重试");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional
    public Result cancelOrder(Long id) {
        String lockKey = ORDER_CANCEL_KEY + id;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                Order order = getById(id);
                if (order == null) {
                    return Result.fail("订单不存在！");
                }
                Long userId = UserHolder.getUser().getId();
                if (!order.getUserId().equals(userId)) {
                    return Result.fail("无权取消该订单！");
                }
                int status = order.getStatus();
                if (status != ORDER_STATUS_PENDING_PAYMENT) {
                    if (status == ORDER_STATUS_PAID || status == ORDER_STATUS_SHIPPED) {
                        return Result.fail("订单已支付！");
                    }
                    return Result.fail("订单状态异常，请稍后重试！");
                }
                LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(OrderItem::getOrderId, id);
                List<OrderItem> items = orderItemService.list(wrapper);
                for (OrderItem item : items) {
                    Long furnitureId = item.getFurnitureId();
                    Furniture furniture = furnitureMapper.selectById(furnitureId);
                    if (furniture == null) {
                        throw new BusinessException("商品不存在，库存恢复失败");
                    }
                    int stockRows = furnitureMapper.incrementStock(furnitureId, item.getQuantity());
                    if (stockRows == 0) {
                        throw new BusinessException("库存恢复失败，请稍后重试");
                    }
                    furniture.setStock(furniture.getStock() + item.getQuantity());
                    updateFurnitureCache(furniture);
                }
                boolean success = update()
                        .set("status", ORDER_STATUS_CANCELLED)
                        .eq("id", id)
                        .eq("status", ORDER_STATUS_PENDING_PAYMENT)
                        .update();
                if (!success) {
                    throw new BusinessException("订单状态更新失败！");
                }
                return Result.ok();
            } else {
                return Result.fail("订单取消处理中，请勿重复操作");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("取消订单获取锁被中断: orderId={}", id, e);
            return Result.fail("系统繁忙，请稍后重试");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional
    public Result confirmReceipt(Long id) {
        String lockKey = ORDER_RECEIVE_KEY + id;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                Long userId = UserHolder.getUser().getId();
                Order order = getById(id);
                if (order == null) {
                    return Result.fail("订单不存在！");
                }
                if (!order.getUserId().equals(userId)) {
                    return Result.fail("无权操作该订单！");
                }
                int status = order.getStatus();
                if (status != ORDER_STATUS_SHIPPED) {
                    if (status == ORDER_STATUS_PENDING_PAYMENT) {
                        return Result.fail("请先支付！");
                    } else if (status == ORDER_STATUS_PAID) {
                        return Result.fail("订单还未发货，请不要随意收货哦！");
                    } else if (status == ORDER_STATUS_COMPLETED || status == ORDER_STATUS_REVIEWED) {
                        return Result.ok();
                    } else {
                        return Result.fail("订单已经取消，请重新下单！");
                    }
                }
                boolean success = update()
                        .set("status", ORDER_STATUS_COMPLETED)
                        .set("receive_time", LocalDateTime.now())
                        .eq("id", id)
                        .eq("status", ORDER_STATUS_SHIPPED)
                        .update();
                if (!success) {
                    Order updated = getById(id);
                    if (updated.getStatus() == ORDER_STATUS_COMPLETED || updated.getStatus() == ORDER_STATUS_REVIEWED) {
                        return Result.ok();
                    }
                    throw new BusinessException("确认收货失败，请稍后重试或联系平台客服！");
                }
                sendOrderStatusMq(order, "order-received", "订单已收货",
                        "您的订单 #" + id + " 已确认收货，感谢您的购买！");
                return Result.ok();
            } else {
                return Result.fail("收货处理中，请勿重复操作");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("确认收货获取锁被中断: orderId={}", id, e);
            return Result.fail("系统繁忙，请稍后重试");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
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

    private void updateFurnitureCache(Furniture furniture) {
        String key = CACHE_FURNITURE_KEY + furniture.getId();
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(cached)) {
            try {
                RedisData redisData = new RedisData();
                redisData.setData(furniture);
                redisData.setExpireTime(LocalDateTime.now().plusSeconds(3600));
                stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
            } catch (Exception e) {
                log.error("更新家具缓存失败: furnitureId={}", furniture.getId(), e);
            }
        }
    }

    private void sendOrderStatusMq(Order order, String type, String title, String content) {
        User user = userMapper.selectById(order.getUserId());
        if (user == null || StrUtil.isBlank(user.getEmail())) {
            return;
        }
        try {
            RocketMQMessage msg = new RocketMQMessage();
            msg.setType(type);
            msg.setOrderId(order.getId());
            msg.setUserId(order.getUserId());
            msg.setUserEmail(user.getEmail());
            msg.setUserName(user.getUserName());
            msg.setTitle(title);
            msg.setContent(content);
            rocketMQTemplate.convertAndSend("order-status-topic", JSONUtil.toJsonStr(msg));
            log.info("订单状态MQ消息已发送: orderId={}, type={}", order.getId(), type);
        } catch (Exception e) {
            log.error("MQ发送失败，回退到直接邮件发送: orderId={}", order.getId(), e);
            emailService.sendNotificationEmail(user.getEmail(), title, content);
        }
    }

}

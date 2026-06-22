package gcy.system.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import gcy.system.entity.dto.*;
import gcy.system.entity.pojo.*;
import gcy.system.entity.vo.OrderItemVO;
import gcy.system.entity.vo.OrderVO;
import gcy.system.exception.BusinessException;
import gcy.system.mapper.*;
import gcy.system.service.EmailService;
import gcy.system.service.IOrderItemService;
import gcy.system.service.IOrderService;
import gcy.system.utils.RedisData;
import gcy.system.utils.UserHolder;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static gcy.system.utils.RedisConstants.*;
import static gcy.system.utils.OrderStatus.*;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

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

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private SkuSpecMapper skuSpecMapper;

    @Resource
    private SpecGroupMapper specGroupMapper;

    @Resource
    private SpecValueMapper specValueMapper;

    /**
     * 执行带分布式锁的操作（使用看门狗自动续期）
     *
     * @param lockKey  锁的key
     * @param waitTime 等待时间（秒）
     * @param action   要执行的操作
     * @return 操作结果
     */
    private Result executeWithLock(String lockKey, long waitTime, Supplier<Result> action) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 不设置 leaseTime，使用看门狗自动续期（默认30秒续期一次）
            boolean locked = lock.tryLock(waitTime, TimeUnit.SECONDS);
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
        return executeWithLock(lockKey, 5, () -> {
            if (StrUtil.isBlank(dto.getConsignee()) || StrUtil.isBlank(dto.getAddress()) || StrUtil.isBlank(dto.getPhone())) {
                return Result.fail("请填写完整的收货信息");
            }
            List<OrderItemDTO> items = dto.getItemList();
            if (items == null || items.isEmpty()) {
                return Result.fail("购物车为空");
            }
            Order order = BeanUtil.copyProperties(dto, Order.class);
            order.setCreateTime(LocalDateTime.now());
            order.setStatus(PENDING_PAYMENT.getCode());
            order.setUserId(userId);
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();
            for (OrderItemDTO itemDto : items) {
                Long furnitureId = itemDto.getFurnitureId();
                Long skuId = itemDto.getSkuId();
                int quantity = itemDto.getQuantity();
                Furniture furniture = furnitureMapper.selectById(furnitureId);
                if (furniture == null) {
                    throw new BusinessException("商品不存在或已下架");
                }

                BigDecimal itemPrice;
                if (skuId != null) {
                    // SKU模式：从SKU扣库存
                    Sku sku = skuMapper.selectById(skuId);
                    if (sku == null || !sku.getFurnitureId().equals(furnitureId)) {
                        throw new BusinessException("商品规格不存在");
                    }
                    if (sku.getStock() < quantity) {
                        throw new BusinessException("商品 " + furniture.getFName() + " 该规格库存不足，当前库存: " + sku.getStock());
                    }
                    int rows = skuMapper.decrementStock(skuId, quantity);
                    if (rows == 0) {
                        throw new BusinessException("商品 " + furniture.getFName() + " 库存发生变化，请重新下单");
                    }
                    itemPrice = sku.getPrice();

                    // 同步更新furniture表的总库存
                    furnitureMapper.incrementStock(furnitureId, -quantity);
                } else {
                    // 兼容旧模式：直接从furniture扣库存
                    if (furniture.getStock() < quantity) {
                        throw new BusinessException("商品 " + furniture.getFName() + " 库存不足，当前库存: " + furniture.getStock());
                    }
                    int rows = furnitureMapper.decrementStock(furnitureId, quantity);
                    if (rows == 0) {
                        throw new BusinessException("商品 " + furniture.getFName() + " 库存发生变化，请重新下单");
                    }
                    itemPrice = furniture.getPrice();
                }

                furniture.setStock(furniture.getStock() - quantity);
                updateFurnitureCache(furniture);
                BigDecimal itemTotal = itemPrice.multiply(new BigDecimal(quantity));
                totalAmount = totalAmount.add(itemTotal);
                OrderItem orderItem = new OrderItem();
                orderItem.setFurnitureId(furnitureId);
                orderItem.setSkuId(skuId);
                orderItem.setPrice(itemPrice);
                orderItem.setQuantity(quantity);
                orderItem.setItemTotalPrice(itemTotal);
                orderItem.setFurnitureName(furniture.getFName());
                orderItem.setFurnitureIcon(furniture.getFIcon());
                // 保存规格快照
                if (skuId != null) {
                    orderItem.setSkuSpec(buildSkuSpecText(skuId));
                }
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
                if (status != PENDING_PAYMENT.getCode()) {
                    if (status == PAID.getCode() || status == SHIPPED.getCode()) {
                        return Result.ok();
                    }
                    return Result.fail("订单状态异常，请稍后重新支付或取消订单！");
                }
                boolean success = update()
                        .set("status", PAID.getCode())
                        .set("pay_time", LocalDateTime.now())
                        .eq("id", id)
                        .eq("status", PENDING_PAYMENT.getCode())
                        .update();
                if (!success) {
                    Order updated = getById(id);
                    if (updated.getStatus() == PAID.getCode() || updated.getStatus() == SHIPPED.getCode()) {
                        return Result.ok();
                    }
                    return Result.fail("支付失败，请重试");
                }
                sendOrderStatusMq(order, "order-paid", "订单支付成功",
                        "您的订单 #" + id + " 已支付成功，我们将尽快为您发货。",
                        "💳", "#27ae60");
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
                if (status != PENDING_PAYMENT.getCode()) {
                    if (status == PAID.getCode() || status == SHIPPED.getCode()) {
                        return Result.fail("订单已支付！");
                    }
                    return Result.fail("订单状态异常，请稍后重试！");
                }
                LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(OrderItem::getOrderId, id);
                List<OrderItem> items = orderItemService.list(wrapper);
                for (OrderItem item : items) {
                    Long furnitureId = item.getFurnitureId();
                    Long skuId = item.getSkuId();
                    Furniture furniture = furnitureMapper.selectById(furnitureId);
                    if (furniture == null) {
                        throw new BusinessException("商品不存在，库存恢复失败");
                    }
                    if (skuId != null) {
                        // SKU模式：恢复SKU库存
                        int stockRows = skuMapper.incrementStock(skuId, item.getQuantity());
                        if (stockRows == 0) {
                            throw new BusinessException("库存恢复失败，请稍后重试");
                        }
                        // 同步恢复furniture表总库存
                        furnitureMapper.incrementStock(furnitureId, item.getQuantity());
                    } else {
                        // 兼容旧模式
                        int stockRows = furnitureMapper.incrementStock(furnitureId, item.getQuantity());
                        if (stockRows == 0) {
                            throw new BusinessException("库存恢复失败，请稍后重试");
                        }
                    }
                    furniture.setStock(furniture.getStock() + item.getQuantity());
                    updateFurnitureCache(furniture);
                }
                boolean success = update()
                        .set("status", CANCELLED.getCode())
                        .eq("id", id)
                        .eq("status", PENDING_PAYMENT.getCode())
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
                if (status != SHIPPED.getCode()) {
                    if (status == PENDING_PAYMENT.getCode()) {
                        return Result.fail("请先支付！");
                    } else if (status == PAID.getCode()) {
                        return Result.fail("订单还未发货，请不要随意收货哦！");
                    } else if (status == COMPLETED.getCode() || status == REVIEWED.getCode()) {
                        return Result.ok();
                    } else {
                        return Result.fail("订单已经取消，请重新下单！");
                    }
                }
                boolean success = update()
                        .set("status", COMPLETED.getCode())
                        .set("receive_time", LocalDateTime.now())
                        .eq("id", id)
                        .eq("status", SHIPPED.getCode())
                        .update();
                if (!success) {
                    Order updated = getById(id);
                    if (updated.getStatus() == COMPLETED.getCode() || updated.getStatus() == REVIEWED.getCode()) {
                        return Result.ok();
                    }
                    throw new BusinessException("确认收货失败，请稍后重试或联系平台客服！");
                }
                sendOrderStatusMq(order, "order-received", "订单已收货",
                        "您的订单 #" + id + " 已确认收货，感谢您的购买！",
                        "✅", "#27ae60");
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

    private String buildSkuSpecText(Long skuId) {
        List<SkuSpec> specs = skuSpecMapper.selectList(
                new LambdaQueryWrapper<SkuSpec>().eq(SkuSpec::getSkuId, skuId));
        if (specs.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < specs.size(); i++) {
            SkuSpec ss = specs.get(i);
            SpecGroup group = specGroupMapper.selectById(ss.getSpecGroupId());
            SpecValue value = specValueMapper.selectById(ss.getSpecValueId());
            if (group != null && value != null) {
                if (i > 0) sb.append(",");
                sb.append(group.getGroupName()).append(":").append(value.getValueName());
            }
        }
        return sb.toString();
    }

    private void sendOrderStatusMq(Order order, String type, String title, String content,
                                   String statusIcon, String statusColor) {
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
            msg.setStatusIcon(statusIcon);
            msg.setStatusColor(statusColor);
            rocketMQTemplate.convertAndSend("order-status-topic", JSONUtil.toJsonStr(msg));
            log.info("订单状态MQ消息已发送: orderId={}, type={}", order.getId(), type);
        } catch (Exception e) {
            log.error("MQ发送失败，回退到直接邮件发送: orderId={}", order.getId(), e);
            emailService.sendOrderStatusEmail(user.getEmail(), order.getId(), title, content,
                    statusIcon, statusColor, order.getTotalPrice().toString(), user.getUserName());
        }
    }

}

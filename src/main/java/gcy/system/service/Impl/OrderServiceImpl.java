package gcy.system.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.dto.CartFormDTO;
import gcy.system.entity.dto.OrderItemDTO;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.UserDTO;
import gcy.system.entity.pojo.*;
import gcy.system.entity.vo.OrderVO;
import gcy.system.exception.BusinessException;
import gcy.system.mapper.*;
import gcy.system.service.IOrderItemService;
import gcy.system.service.IOrderService;
import gcy.system.utils.LockUtil;
import gcy.system.utils.OrderMqHelper;
import gcy.system.utils.RedisData;
import gcy.system.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static gcy.system.utils.OrderStatus.*;
import static gcy.system.utils.RedisConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final FurnitureMapper furnitureMapper;

    private final IOrderItemService orderItemService;

    private final StringRedisTemplate stringRedisTemplate;

    private final OrderMqHelper orderMqHelper;

    private final SkuMapper skuMapper;

    private final SkuSpecMapper skuSpecMapper;

    private final SpecGroupMapper specGroupMapper;

    private final SpecValueMapper specValueMapper;

    private final RedissonClient redissonClient;

    @Override
    @Transactional
    public Result createOrder(CartFormDTO dto) {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();
        String lockKey = ORDER_CREATE_KEY + userId;
        return LockUtil.executeWithLock(redissonClient, lockKey, 5, () -> {
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
                if (quantity <= 0) {
                    throw new BusinessException("商品数量必须大于0");
                }
                Furniture furniture = furnitureMapper.selectById(furnitureId);
                if (furniture == null) {
                    throw new BusinessException("商品不存在或已下架");
                }
                BigDecimal itemPrice;
                if (skuId != null) {
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
                    furnitureMapper.decrementStock(furnitureId, quantity);
                } else {
                    // 检查该商品是否有规格SKU，有则必须选择具体规格
                    if (skuMapper.selectCount(
                            new LambdaQueryWrapper<Sku>().eq(Sku::getFurnitureId, furnitureId)) > 0) {
                        throw new BusinessException("商品「" + furniture.getFName() + "」有多个规格，请选择具体规格后下单");
                    }
                    if (furniture.getStock() < quantity) {
                        throw new BusinessException("商品 " + furniture.getFName() + " 库存不足，当前库存: " + furniture.getStock());
                    }
                    int rows = furnitureMapper.decrementStock(furnitureId, quantity);
                    if (rows == 0) {
                        throw new BusinessException("商品 " + furniture.getFName() + " 库存发生变化，请重新下单");
                    }
                    itemPrice = furniture.getPrice();
                }

                // 重新查询最新库存，避免使用读取前的过期值更新缓存
                Furniture latestFurniture = furnitureMapper.selectById(furnitureId);
                if (latestFurniture != null) {
                    updateFurnitureCache(latestFurniture);
                }
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
                .map(order -> OrderVO.from(order, itemMap.getOrDefault(order.getId(), Collections.emptyList())))
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
        return LockUtil.executeWithLock(redissonClient, ORDER_PAY_KEY + id, 5, () -> {
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
            orderMqHelper.send("order-status-topic", order, "order-paid", "订单支付成功",
                    "您的订单 #" + id + " 已支付成功，我们将尽快为您发货。",
                    "💳", "#27ae60");
            return Result.ok();
        });
    }

    @Override
    @Transactional
    public Result cancelOrder(Long id) {
        return LockUtil.executeWithLock(redissonClient, ORDER_CANCEL_KEY + id, 5, () -> {
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
            doCancelOrder(id);
            return Result.ok();
        });
    }

    /**
     * 系统自动取消超时未支付订单（无用户上下文，跳过归属校验）。
     */
    @Transactional
    public Result cancelTimeoutOrder(Long id) {
        return LockUtil.executeWithLock(redissonClient, ORDER_CANCEL_KEY + id, 5, () -> {
            Order order = getById(id);
            if (order == null) {
                return Result.fail("订单不存在");
            }
            if (order.getStatus() != PENDING_PAYMENT.getCode()) {
                log.info("超时取消时订单状态已变更，跳过: orderId={}, status={}", id, order.getStatus());
                return Result.ok();
            }
            doCancelOrder(id);
            log.info("超时未支付订单已自动取消: orderId={}, userId={}", id, order.getUserId());
            return Result.ok();
        });
    }

    /**
     * 取消订单的核心操作：恢复库存 + 更新状态。
     * 调用方需自行完成权限校验和锁控制。
     */
    private void doCancelOrder(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
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
            // 重新查询最新库存，避免使用读取前的过期值更新缓存
            Furniture latestFurniture = furnitureMapper.selectById(furnitureId);
            if (latestFurniture != null) {
                updateFurnitureCache(latestFurniture);
            }
        }
        boolean success = update()
                .set("status", CANCELLED.getCode())
                .eq("id", orderId)
                .eq("status", PENDING_PAYMENT.getCode())
                .update();
        if (!success) {
            throw new BusinessException("订单状态更新失败！");
        }
    }

    @Override
    @Transactional
    public Result confirmReceipt(Long id) {
        return LockUtil.executeWithLock(redissonClient, ORDER_RECEIVE_KEY + id, 5, () -> {
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
            orderMqHelper.send("order-status-topic", order, "order-received", "订单已收货",
                    "您的订单 #" + id + " 已确认收货，感谢您的购买！",
                    "✅", "#27ae60");
            return Result.ok();
        });
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

        // 批量加载规格组和规格值，避免循环内逐条 selectById（N+1）
        List<Long> groupIds = specs.stream().map(SkuSpec::getSpecGroupId).distinct().collect(Collectors.toList());
        List<Long> valueIds = specs.stream().map(SkuSpec::getSpecValueId).distinct().collect(Collectors.toList());
        Map<Long, String> groupNames = specGroupMapper.selectBatchIds(groupIds).stream()
                .collect(Collectors.toMap(SpecGroup::getId, SpecGroup::getGroupName));
        Map<Long, String> valueNames = specValueMapper.selectBatchIds(valueIds).stream()
                .collect(Collectors.toMap(SpecValue::getId, SpecValue::getValueName));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < specs.size(); i++) {
            SkuSpec ss = specs.get(i);
            String groupName = groupNames.get(ss.getSpecGroupId());
            String valueName = valueNames.get(ss.getSpecValueId());
            if (groupName != null && valueName != null) {
                if (i > 0) sb.append(",");
                sb.append(groupName).append(":").append(valueName);
            }
        }
        return sb.toString();
    }
}


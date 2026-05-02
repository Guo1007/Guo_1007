package com.example.furnituresystem.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.furnituresystem.entity.dto.CartFormDTO;
import com.example.furnituresystem.entity.dto.OrderItemDTO;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.dto.UserDTO;
import com.example.furnituresystem.entity.pojo.Furniture;
import com.example.furnituresystem.entity.pojo.Order;
import com.example.furnituresystem.entity.pojo.OrderItem;
import com.example.furnituresystem.entity.vo.OrderItemVO;
import com.example.furnituresystem.entity.vo.OrderVO;
import com.example.furnituresystem.exception.BusinessException;
import com.example.furnituresystem.mapper.FurnitureMapper;
import com.example.furnituresystem.mapper.OrderMapper;
import com.example.furnituresystem.service.IFurnitureService;
import com.example.furnituresystem.service.IOrderItemService;
import com.example.furnituresystem.service.IOrderService;
import com.example.furnituresystem.utils.RedisData;
import com.example.furnituresystem.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.furnituresystem.utils.RedisConstants.CACHE_FURNITURE_KEY;

@Service
public class IOrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Resource
    private FurnitureMapper furnitureMapper;

    @Resource
    private IFurnitureService furnitureService;

    @Resource
    private IOrderItemService orderItemService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public Result createOrder(CartFormDTO dto) {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();
        if (StrUtil.isBlank(dto.getConsignee()) || StrUtil.isBlank(dto.getAddress()) || StrUtil.isBlank(dto.getPhone())) {
            return Result.fail("请填写完整的收货信息");
        }
        List<OrderItemDTO> items = dto.getItemList();
        if (items == null || items.isEmpty()) {
            return Result.fail("购物车为空");
        }
        Order order = BeanUtil.copyProperties(dto, Order.class);
        order.setCreateTime(LocalDateTime.now());
        order.setStatus(0);
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
            boolean success = furnitureService.update()
                    .setSql("stock = stock - " + quantity)
                    .eq("id", furnitureId)
                    .ge("stock", quantity)
                    .update();
            if (!success) {
                throw new BusinessException("商品 " + furniture.getFName() + " 库存不足，手慢无！");
            }
            Furniture updatedFurniture = furnitureMapper.selectById(furnitureId);
            String cacheFurniture = stringRedisTemplate.opsForValue()
                    .get(CACHE_FURNITURE_KEY + furnitureId);
            if (StrUtil.isNotBlank(cacheFurniture)) {
                RedisData redisData = new RedisData();
                redisData.setData(updatedFurniture);
                redisData.setExpireTime(LocalDateTime.now().plusSeconds(3600));
                stringRedisTemplate.opsForValue()
                        .set(CACHE_FURNITURE_KEY + furnitureId, JSONUtil.toJsonStr(redisData));
            }
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
    public Result payOrderById(Long id) {
        Order order = getById(id);
        if (order == null) {
            return Result.fail("订单不存在！");
        }
        Long userId = UserHolder.getUser().getId();
        int status = order.getStatus();
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权支付该订单！");
        }
        if (status != 0) {
            if (status == 1 || status == 2) {
                return Result.fail("该订单已经支付！");
            }
            return Result.fail("订单状态异常，请稍后重新支付或取消订单！");
        }
        boolean success = update()
                .set("status", 1)
                .set("pay_time", LocalDateTime.now())
                .eq("id", id)
                .eq("status", 0)
                .update();
        if (!success) {
            Order updated = getById(id);
            if (updated.getStatus() == 1 || updated.getStatus() == 2) {
                return Result.ok();
            }
            return Result.fail("支付失败，请重试");
        }
        return Result.ok();
    }

    @Override
    @Transactional
    public Result cancelOrder(Long id) {
        Order order = getById(id);
        if (order == null) {
            return Result.fail("订单不存在！");
        }
        Long userId = UserHolder.getUser().getId();
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权取消该订单！");
        }
        int status = order.getStatus();
        if (status != 0) {
            if (status == 1 || status == 2) {
                return Result.fail("订单已支付！");
            }
            return Result.fail("订单状态异常，请稍后重试！");
        }
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, id);
        List<OrderItem> items = orderItemService.list(wrapper);
        for (OrderItem item : items) {
            Long furnitureId = item.getFurnitureId();
            boolean stockSuccess = furnitureService.update()
                    .setSql("stock = stock + " + item.getQuantity())
                    .eq("id", item.getFurnitureId())
                    .update();
            if (!stockSuccess) {
                throw new BusinessException("库存恢复失败，请稍后重试");
            }
            Furniture furniture = furnitureMapper.selectById(furnitureId);
            String cacheFurniture = stringRedisTemplate.opsForValue()
                    .get(CACHE_FURNITURE_KEY + furnitureId);
            if (StrUtil.isNotBlank(cacheFurniture)) {
                RedisData redisData = new RedisData();
                redisData.setData(furniture);
                redisData.setExpireTime(LocalDateTime.now().plusSeconds(3600));
                stringRedisTemplate.opsForValue()
                        .set(CACHE_FURNITURE_KEY + furnitureId, JSONUtil.toJsonStr(redisData));
            }
        }
        boolean success = update()
                .set("status", 4)
                .eq("id", id)
                .eq("status", 0)
                .update();
        if (!success) {
            throw new BusinessException("订单状态更新失败！");
        }
        return Result.ok();
    }

    @Override
    @Transactional
    public Result confirmReceipt(Long id) {
        Order order = getById(id);
        if (order == null) {
            return Result.fail("订单不存在！");
        }
        int status = order.getStatus();
        if (status != 2) {
            if (status == 0) {
                return Result.fail("请先支付！");
            } else if (status == 1) {
                return Result.fail("订单还未发货，请不要随意收货哦！");
            } else if (status == 3) {
                return Result.fail("订单已收货，无需重复收货哦！");
            } else {
                return Result.fail("订单已经取消，请重新下单！");
            }
        }
        order.setStatus(3);
        boolean success = updateById(order);
        if (!success) {
            throw new BusinessException("确认收货失败，请稍后重试或联系平台客服！");
        }
        return Result.ok();
    }

}

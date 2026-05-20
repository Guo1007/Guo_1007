package com.example.furnituresystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.pojo.Order;
import com.example.furnituresystem.entity.pojo.OrderItem;
import com.example.furnituresystem.entity.pojo.Review;
import com.example.furnituresystem.exception.BusinessException;
import com.example.furnituresystem.mapper.OrderItemMapper;
import com.example.furnituresystem.mapper.OrderMapper;
import com.example.furnituresystem.mapper.ReviewMapper;
import com.example.furnituresystem.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @Resource
    private ReviewMapper reviewMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderItemMapper orderItemMapper;

    @GetMapping("/list/{furnitureId}")
    public Result list(@PathVariable Long furnitureId) {
        List<Map<String, Object>> reviews = reviewMapper.selectReviewsByFurnitureId(furnitureId);
        Map<String, Object> stats = reviewMapper.selectRatingStats(furnitureId);
        return Result.ok(Map.of("reviews", reviews, "stats", stats));
    }

    @GetMapping("/order/{orderId}")
    public Result listByOrder(@PathVariable Long orderId) {
        Long userId = UserHolder.getUser().getId();
        List<Review> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getUserId, userId)
                        .eq(Review::getOrderId, orderId)
                        .orderByDesc(Review::getCreateTime)
        );
        return Result.ok(reviews);
    }

    @PostMapping("/add")
    @Transactional
    public Result add(@RequestBody Review review) {
        Long userId = UserHolder.getUser().getId();
        if (review.getOrderId() == null) {
            throw new BusinessException("订单ID不能为空");
        }
        if (review.getFurnitureId() == null) {
            throw new BusinessException("家具ID不能为空");
        }

        Order order = orderMapper.selectById(review.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权评价此订单");
        }
        if (order.getStatus() != 3 && order.getStatus() != 5) {
            throw new BusinessException("请确认收货后再评价");
        }

        boolean exists = reviewMapper.exists(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getUserId, userId)
                        .eq(Review::getOrderId, review.getOrderId())
                        .eq(Review::getFurnitureId, review.getFurnitureId())
        );
        if (exists) {
            throw new BusinessException("您已评价过该商品");
        }

        review.setUserId(userId);
        review.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(review);

        // 检查是否全部商品都已评价，是则更新订单状态为已评价
        long itemCount = orderItemMapper.selectCount(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, review.getOrderId())
        );
        long reviewCount = reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getUserId, userId)
                        .eq(Review::getOrderId, review.getOrderId())
        );
        if (reviewCount >= itemCount && order.getStatus() == 3) {
            orderMapper.update(null,
                    new LambdaUpdateWrapper<Order>()
                            .eq(Order::getId, review.getOrderId())
                            .eq(Order::getStatus, 3)
                            .set(Order::getStatus, 5));
        }

        return Result.ok();
    }

    @DeleteMapping("/{reviewId}")
    public Result delete(@PathVariable Long reviewId) {
        Long userId = UserHolder.getUser().getId();
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException("无权删除他人评价");
        }
        reviewMapper.deleteById(reviewId);

        // 删除后检查订单是否仍需保持已评价状态
        Order order = orderMapper.selectById(review.getOrderId());
        if (order != null && order.getStatus() == 5) {
            long itemCount = orderItemMapper.selectCount(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, review.getOrderId())
            );
            long reviewCount = reviewMapper.selectCount(
                    new LambdaQueryWrapper<Review>()
                            .eq(Review::getUserId, userId)
                            .eq(Review::getOrderId, review.getOrderId())
            );
            if (reviewCount < itemCount) {
                orderMapper.update(null,
                        new LambdaUpdateWrapper<Order>()
                                .eq(Order::getId, review.getOrderId())
                                .eq(Order::getStatus, 5)
                                .set(Order::getStatus, 3));
            }
        }

        return Result.ok();
    }
}

package gcy.system.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import gcy.system.entity.pojo.Order;
import gcy.system.entity.pojo.OrderItem;
import gcy.system.entity.pojo.Review;
import gcy.system.entity.dto.Result;
import gcy.system.exception.BusinessException;
import gcy.system.mapper.OrderItemMapper;
import gcy.system.mapper.OrderMapper;
import gcy.system.mapper.ReviewMapper;
import gcy.system.service.IReviewService;
import gcy.system.utils.OrderStatus;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements IReviewService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderItemMapper orderItemMapper;

    @Resource
    private ReviewMapper reviewMapper;

    @Override
    public Result getReviewsByFurnitureId(Long furnitureId) {
        List<Map<String, Object>> reviews = reviewMapper.selectReviewsByFurnitureId(furnitureId);
        Map<String, Object> stats = reviewMapper.selectRatingStats(furnitureId);
        return Result.ok(Map.of("reviews", reviews, "stats", stats));
    }

    @Override
    public Result getReviewsByOrderId(Long orderId, Long userId) {
        List<Review> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getUserId, userId)
                        .eq(Review::getOrderId, orderId)
                        .orderByDesc(Review::getCreateTime)
        );
        return Result.ok(reviews);
    }

    @Override
    @Transactional
    public Result addReview(Review review, Long userId) {
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
        if (order.getStatus() != OrderStatus.COMPLETED.getCode() 
                && order.getStatus() != OrderStatus.REVIEWED.getCode()) {
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
        if (reviewCount >= itemCount && order.getStatus() == OrderStatus.COMPLETED.getCode()) {
            orderMapper.update(null,
                    new LambdaUpdateWrapper<Order>()
                            .eq(Order::getId, review.getOrderId())
                            .eq(Order::getStatus, OrderStatus.COMPLETED.getCode())
                            .set(Order::getStatus, OrderStatus.REVIEWED.getCode()));
        }

        return Result.ok();
    }

    @Override
    @Transactional
    public Result deleteReview(Long reviewId, Long userId) {
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
        if (order != null && order.getStatus() == OrderStatus.REVIEWED.getCode()) {
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
                                .eq(Order::getStatus, OrderStatus.REVIEWED.getCode())
                                .set(Order::getStatus, OrderStatus.COMPLETED.getCode()));
            }
        }

        return Result.ok();
    }
}

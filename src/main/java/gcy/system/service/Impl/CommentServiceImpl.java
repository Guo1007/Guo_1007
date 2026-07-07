package gcy.system.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.CommentAppend;
import gcy.system.entity.pojo.GoodsComment;
import gcy.system.entity.pojo.Order;
import gcy.system.entity.pojo.OrderItem;
import gcy.system.entity.pojo.ReviewComment;
import gcy.system.entity.vo.CommentAppendVO;
import gcy.system.entity.vo.CommentVO;
import gcy.system.exception.BusinessException;
import gcy.system.mapper.CommentAppendMapper;
import gcy.system.mapper.GoodsCommentMapper;
import gcy.system.mapper.OrderItemMapper;
import gcy.system.mapper.OrderMapper;
import gcy.system.mapper.ReviewCommentMapper;
import gcy.system.service.ICommentService;
import gcy.system.utils.LockUtil;
import gcy.system.utils.OrderStatus;

import static gcy.system.utils.RedisConstants.LOCK_COMMENT_APPEND_KEY;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ICommentService {

    private final GoodsCommentMapper goodsCommentMapper;

    private final CommentAppendMapper commentAppendMapper;

    private final OrderMapper orderMapper;

    private final OrderItemMapper orderItemMapper;

    private final ReviewCommentMapper reviewCommentMapper;

    private final RedissonClient redissonClient;

    @Override
    public Result getCommentsByGoodsId(Long goodsId, Long userId, Integer current, Integer size) {
        Page<CommentVO> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        Page<CommentVO> result = goodsCommentMapper.selectCommentsByGoodsId(goodsId, userId, page);
        fillAppendList(result.getRecords(), userId);
        return Result.ok(result);
    }

    @Override
    public Result getCommentsByOrderId(Long orderId, Long userId) {
        List<CommentVO> comments = goodsCommentMapper.selectCommentsByOrderId(orderId, userId);
        fillAppendList(comments, userId);
        return Result.ok(comments);
    }

    private void fillAppendList(List<CommentVO> comments, Long userId) {
        if (comments.isEmpty()) return;
        List<Long> commentIds = comments.stream().map(CommentVO::getId).collect(Collectors.toList());
        List<CommentAppendVO> allAppends = commentAppendMapper.selectByMainCommentIds(commentIds, userId);
        Map<Long, List<CommentAppendVO>> appendMap = allAppends.stream()
                .collect(Collectors.groupingBy(CommentAppendVO::getMainCommentId));
        for (CommentVO comment : comments) {
            comment.setAppendList(appendMap.getOrDefault(comment.getId(), Collections.emptyList()));
        }
    }

    @Override
    @Transactional
    public Result addComment(GoodsComment comment, Long userId) {
        // 校验订单归属：只能评价自己的订单
        Order order = orderMapper.selectById(comment.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权评价该订单");
        }
        // 已完成/已评价的订单才允许评价
        if (order.getStatus() != OrderStatus.COMPLETED.getCode()
                && order.getStatus() != OrderStatus.REVIEWED.getCode()) {
            throw new BusinessException("订单状态不允许评价");
        }
        GoodsComment existing = goodsCommentMapper.selectByOrderAndGoods(
                comment.getOrderId(), userId, comment.getGoodsId());
        if (existing != null) {
            throw new BusinessException("您已评价过该商品");
        }
        comment.setUserId(userId);
        comment.setStatus(0);
        comment.setHasAppend(0);
        comment.setCreateTime(LocalDateTime.now());
        goodsCommentMapper.insert(comment);
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, comment.getOrderId()));
        List<GoodsComment> existingComments = goodsCommentMapper.selectList(
                new LambdaQueryWrapper<GoodsComment>()
                        .eq(GoodsComment::getOrderId, comment.getOrderId())
                        .eq(GoodsComment::getUserId, userId));
        Set<Long> reviewedGoodsIds = existingComments.stream()
                .map(GoodsComment::getGoodsId).collect(Collectors.toSet());
        boolean allReviewed = !orderItems.isEmpty() && orderItems.stream()
                .allMatch(item -> reviewedGoodsIds.contains(item.getFurnitureId()));
        if (allReviewed) {
            orderMapper.update(null,
                    new LambdaUpdateWrapper<Order>()
                            .eq(Order::getId, comment.getOrderId())
                            .set(Order::getStatus, OrderStatus.REVIEWED.getCode()));
        } else {
            orderMapper.update(null,
                    new LambdaUpdateWrapper<Order>()
                            .eq(Order::getId, comment.getOrderId())
                            .set(Order::getStatus, OrderStatus.COMPLETED.getCode()));
        }
        return Result.ok();
    }

    @Override
    @Transactional
    public Result appendComment(CommentAppend append, Long userId) {
        Result result = LockUtil.executeWithLock(redissonClient,
                LOCK_COMMENT_APPEND_KEY + append.getMainCommentId(), 5, () -> {
            GoodsComment mainComment = goodsCommentMapper.selectById(append.getMainCommentId());
            if (mainComment == null) {
                throw new BusinessException("评价不存在");
            }
            if (!mainComment.getUserId().equals(userId)) {
                throw new BusinessException("只能追评自己的评价");
            }
            int appendCount = commentAppendMapper.countByMainCommentId(append.getMainCommentId());
            append.setUserId(userId);
            append.setAppendNum(appendCount + 1);
            append.setStatus(0);
            append.setAppendTime(LocalDateTime.now());
            commentAppendMapper.insert(append);
            goodsCommentMapper.update(null,
                    new LambdaUpdateWrapper<GoodsComment>()
                            .eq(GoodsComment::getId, append.getMainCommentId())
                            .set(GoodsComment::getHasAppend, 1)
                            .set(GoodsComment::getLatestAppendTime, LocalDateTime.now()));
            return Result.ok();
        });
        if (!result.getSuccess()) {
            throw new BusinessException(result.getErrorMsg());
        }
        return result;
    }

    @Override
    @Transactional
    public Result deleteComment(Long commentId, Long userId) {
        GoodsComment comment = goodsCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评价不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的评价");
        }
        goodsCommentMapper.deleteById(commentId);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result deleteAppend(Long appendId, Long userId) {
        CommentAppend append = commentAppendMapper.selectById(appendId);
        if (append == null) {
            throw new BusinessException("追评不存在");
        }
        if (!append.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的追评");
        }
        commentAppendMapper.deleteById(appendId);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result deleteReview(Long reviewId, Long userId) {
        GoodsComment review = goodsCommentMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的评价");
        }
        commentAppendMapper.delete(
                new LambdaQueryWrapper<CommentAppend>()
                        .eq(CommentAppend::getMainCommentId, reviewId));
        reviewCommentMapper.delete(
                new LambdaQueryWrapper<ReviewComment>()
                        .eq(ReviewComment::getReviewId, reviewId));
        goodsCommentMapper.deleteById(reviewId);
        return Result.ok();
    }
}

package gcy.system.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.*;
import gcy.system.entity.vo.CommentAppendVO;
import gcy.system.entity.vo.CommentVO;
import gcy.system.exception.BusinessException;
import gcy.system.mapper.*;
import gcy.system.service.ICommentService;
import gcy.system.utils.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        // 校验 goodsId 属于该订单
        if (orderItemMapper.selectCount(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, comment.getOrderId())
                        .eq(OrderItem::getFurnitureId, comment.getGoodsId())) == 0) {
            throw new BusinessException("该商品不在该订单中");
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
        try {
            goodsCommentMapper.insert(comment);
        } catch (DuplicateKeyException e) {
            // 软删除后再次评价会被 uk_order_user_goods 唯一索引拦截
            throw new BusinessException("您已评价过该商品");
        }
        log.info("发表评价: commentId={}, orderId={}, goodsId={}, userId={}",
                comment.getId(), comment.getOrderId(), comment.getGoodsId(), userId);
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
        try {
            commentAppendMapper.insert(append);
        } catch (DuplicateKeyException e) {
            // 并发追评导致 appendNum 冲突（极低概率），提示重试
            log.debug("追评序号冲突: mainCommentId={}", append.getMainCommentId());
            throw new BusinessException("操作繁忙，请稍后重试");
        }
        goodsCommentMapper.update(null,
                new LambdaUpdateWrapper<GoodsComment>()
                        .eq(GoodsComment::getId, append.getMainCommentId())
                        .set(GoodsComment::getHasAppend, 1)
                        .set(GoodsComment::getLatestAppendTime, LocalDateTime.now()));
        return Result.ok();
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
        goodsCommentMapper.update(null,
                new LambdaUpdateWrapper<GoodsComment>()
                        .eq(GoodsComment::getId, commentId)
                        .set(GoodsComment::getUserDeleted, 1));
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
        commentAppendMapper.update(null,
                new LambdaUpdateWrapper<CommentAppend>()
                        .eq(CommentAppend::getId, appendId)
                        .set(CommentAppend::getUserDeleted, 1));
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
        commentAppendMapper.update(null,
                new LambdaUpdateWrapper<CommentAppend>()
                        .eq(CommentAppend::getMainCommentId, reviewId)
                        .set(CommentAppend::getUserDeleted, 1));
        reviewCommentMapper.update(null,
                new LambdaUpdateWrapper<ReviewComment>()
                        .eq(ReviewComment::getReviewId, reviewId)
                        .set(ReviewComment::getUserDeleted, 1));
        goodsCommentMapper.update(null,
                new LambdaUpdateWrapper<GoodsComment>()
                        .eq(GoodsComment::getId, reviewId)
                        .set(GoodsComment::getUserDeleted, 1));
        return Result.ok();
    }
}

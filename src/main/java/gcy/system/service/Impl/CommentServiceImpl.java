package gcy.system.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.CommentAppend;
import gcy.system.entity.pojo.GoodsComment;
import gcy.system.entity.pojo.Order;
import gcy.system.entity.pojo.ReviewComment;
import gcy.system.entity.vo.CommentAppendVO;
import gcy.system.entity.vo.CommentVO;
import gcy.system.exception.BusinessException;
import gcy.system.mapper.CommentAppendMapper;
import gcy.system.mapper.GoodsCommentMapper;
import gcy.system.mapper.OrderMapper;
import gcy.system.mapper.ReviewCommentMapper;
import gcy.system.service.ICommentService;
import gcy.system.utils.JvmLockManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ICommentService {

    private final GoodsCommentMapper goodsCommentMapper;

    private final CommentAppendMapper commentAppendMapper;

    private final OrderMapper orderMapper;

    private final ReviewCommentMapper reviewCommentMapper;

    @Override
    public Result getCommentsByGoodsId(Long goodsId, Long userId, Integer current, Integer size) {
        Page<CommentVO> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        Page<CommentVO> result = goodsCommentMapper.selectCommentsByGoodsId(goodsId, userId, page);
        List<CommentVO> records = result.getRecords();
        for (CommentVO comment : records) {
            List<CommentAppendVO> appendList = commentAppendMapper.selectByMainCommentId(comment.getId(), userId);
            comment.setAppendList(appendList);
        }
        return Result.ok(result);
    }

    @Override
    public Result getCommentsByOrderId(Long orderId, Long userId) {
        List<CommentVO> comments = goodsCommentMapper.selectCommentsByOrderId(orderId, userId);
        for (CommentVO comment : comments) {
            List<CommentAppendVO> appendList = commentAppendMapper.selectByMainCommentId(comment.getId(), userId);
            comment.setAppendList(appendList);
        }
        return Result.ok(comments);
    }

    @Override
    @Transactional
    public Result addComment(GoodsComment comment, Long userId) {
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
        orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, comment.getOrderId())
                        .set(Order::getStatus, 5));
        return Result.ok();
    }

    @Override
    @Transactional
    public Result appendComment(CommentAppend append, Long userId) {
        String lockKey = "lock:comment:append:" + append.getMainCommentId();
        ReentrantLock lock = JvmLockManager.getLock(lockKey);
        try {
            if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                throw new BusinessException("操作处理中，请稍后再试");
            }
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
            append.setStatus(0); // 待审核
            append.setAppendTime(LocalDateTime.now());
            commentAppendMapper.insert(append);
            goodsCommentMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<GoodsComment>()
                            .eq(GoodsComment::getId, append.getMainCommentId())
                            .set(GoodsComment::getHasAppend, 1)
                            .set(GoodsComment::getLatestAppendTime, LocalDateTime.now()));
            return Result.ok();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统繁忙，请稍后再试");
        } finally {
            lock.unlock();
        }
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

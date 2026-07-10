package gcy.system.service.admin.Impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.CommentAppend;
import gcy.system.entity.pojo.GoodsComment;
import gcy.system.entity.pojo.ReviewComment;
import gcy.system.entity.pojo.User;
import gcy.system.entity.vo.admin.AdminAppendVO;
import gcy.system.entity.vo.admin.AdminCommentVO;
import gcy.system.entity.vo.admin.AdminReviewCommentVO;
import gcy.system.exception.BusinessException;
import gcy.system.listener.CommentReplyListener;
import gcy.system.mapper.CommentAppendMapper;
import gcy.system.mapper.GoodsCommentMapper;
import gcy.system.mapper.ReviewCommentMapper;
import gcy.system.mapper.UserMapper;
import gcy.system.mapper.admin.CommentManageMapper;
import gcy.system.service.admin.ICommentManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentManageServiceImpl implements ICommentManageService {

    private final GoodsCommentMapper goodsCommentMapper;

    private final CommentAppendMapper commentAppendMapper;

    private final ReviewCommentMapper reviewCommentMapper;

    private final CommentManageMapper commentManageMapper;

    private final RocketMQTemplate rocketMQTemplate;

    private final UserMapper userMapper;

    @Override
    public Result getAllComments(Integer current, Integer size) {
        Page<AdminCommentVO> page = new Page<>(current, size);
        Page<AdminCommentVO> result = commentManageMapper.selectAllComments(page);
        return Result.ok(result);
    }

    @Override
    @Transactional
    public Result approveComment(Long commentId) {
        GoodsComment comment = goodsCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评价不存在");
        }
        goodsCommentMapper.update(null,
                new LambdaUpdateWrapper<GoodsComment>()
                        .eq(GoodsComment::getId, commentId)
                        .set(GoodsComment::getStatus, 1));
        return Result.ok();
    }

    @Override
    @Transactional
    public Result rejectComment(Long commentId) {
        GoodsComment comment = goodsCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评价不存在");
        }
        goodsCommentMapper.update(null,
                new LambdaUpdateWrapper<GoodsComment>()
                        .eq(GoodsComment::getId, commentId)
                        .set(GoodsComment::getStatus, 2));
        return Result.ok();
    }

    @Override
    public Result getAllAppends(Integer current, Integer size) {
        Page<AdminAppendVO> page = new Page<>(current, size);
        Page<AdminAppendVO> result = commentManageMapper.selectAllAppends(page);
        return Result.ok(result);
    }

    @Override
    @Transactional
    public Result approveAppend(Long appendId) {
        CommentAppend append = commentAppendMapper.selectById(appendId);
        if (append == null) {
            throw new BusinessException("追评不存在");
        }
        commentAppendMapper.update(null,
                new LambdaUpdateWrapper<CommentAppend>()
                        .eq(CommentAppend::getId, appendId)
                        .set(CommentAppend::getStatus, 1));
        return Result.ok();
    }

    @Override
    @Transactional
    public Result rejectAppend(Long appendId) {
        CommentAppend append = commentAppendMapper.selectById(appendId);
        if (append == null) {
            throw new BusinessException("追评不存在");
        }
        commentAppendMapper.update(null,
                new LambdaUpdateWrapper<CommentAppend>()
                        .eq(CommentAppend::getId, appendId)
                        .set(CommentAppend::getStatus, 2));
        return Result.ok();
    }

    @Override
    public Result getAllReviewComments(Integer current, Integer size) {
        Page<AdminReviewCommentVO> page = new Page<>(current, size);
        Page<AdminReviewCommentVO> result = commentManageMapper.selectAllReviewComments(page);
        return Result.ok(result);
    }

    @Override
    @Transactional
    public Result approveReviewComment(Long commentId) {
        ReviewComment comment = reviewCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        reviewCommentMapper.update(null,
                new LambdaUpdateWrapper<ReviewComment>()
                        .eq(ReviewComment::getId, commentId)
                        .set(ReviewComment::getStatus, 1));
        if (comment.getReplyToUserId() != null && !comment.getReplyToUserId().equals(comment.getUserId())) {
            try {
                User replyUser = userMapper.selectById(comment.getUserId());
                String userName = replyUser != null ? replyUser.getUserName() : "用户";
                GoodsComment goodsComment = goodsCommentMapper.selectById(comment.getReviewId());
                Long goodsId = goodsComment != null ? goodsComment.getGoodsId() : null;
                CommentReplyListener.CommentReplyMessage msg = new CommentReplyListener.CommentReplyMessage(
                        comment.getReplyToUserId(),
                        comment.getReviewId(),
                        goodsId,
                        comment.getId(),
                        comment.getUserId(),
                        userName,
                        userName + " 回复了你的评论"
                );
                rocketMQTemplate.convertAndSend("comment-reply-topic", JSONUtil.toJsonStr(msg));
                log.info("评论回复通知已发送: targetUserId={}", comment.getReplyToUserId());
            } catch (Exception e) {
                log.error("发送评论回复通知失败", e);
            }
        }
        return Result.ok();
    }

    @Override
    @Transactional
    public Result rejectReviewComment(Long commentId) {
        ReviewComment comment = reviewCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        reviewCommentMapper.update(null,
                new LambdaUpdateWrapper<ReviewComment>()
                        .eq(ReviewComment::getId, commentId)
                        .set(ReviewComment::getStatus, 2));
        return Result.ok();
    }

    // ========== 删除（软删除 deleted=1） ==========

    @Override
    @Transactional
    public Result deleteComment(Long id) {
        // 级联软删除关联的追评和评论
        commentAppendMapper.update(null,
                new LambdaUpdateWrapper<CommentAppend>()
                        .eq(CommentAppend::getMainCommentId, id)
                        .set(CommentAppend::getDeleted, 1));
        reviewCommentMapper.update(null,
                new LambdaUpdateWrapper<ReviewComment>()
                        .eq(ReviewComment::getReviewId, id)
                        .set(ReviewComment::getDeleted, 1));
        goodsCommentMapper.deleteById(id);
        log.info("管理员删除评价及关联数据: commentId={}", id);
        return Result.okMsg("删除成功");
    }

    @Override
    @Transactional
    public Result batchDeleteComments(List<Long> ids) {
        for (Long id : ids) {
            commentAppendMapper.update(null,
                    new LambdaUpdateWrapper<CommentAppend>()
                            .eq(CommentAppend::getMainCommentId, id)
                            .set(CommentAppend::getDeleted, 1));
            reviewCommentMapper.update(null,
                    new LambdaUpdateWrapper<ReviewComment>()
                            .eq(ReviewComment::getReviewId, id)
                            .set(ReviewComment::getDeleted, 1));
        }
        goodsCommentMapper.deleteByIds(ids);
        log.info("管理员批量删除评价及关联数据: count={}", ids.size());
        return Result.okMsg("批量删除成功");
    }

    @Override
    @Transactional
    public Result deleteAppend(Long id) {
        commentAppendMapper.deleteById(id);
        log.info("管理员删除追评: appendId={}", id);
        return Result.okMsg("删除成功");
    }

    @Override
    @Transactional
    public Result batchDeleteAppends(List<Long> ids) {
        commentAppendMapper.deleteByIds(ids);
        log.info("管理员批量删除追评: count={}", ids.size());
        return Result.okMsg("批量删除成功");
    }

    @Override
    @Transactional
    public Result deleteReviewComment(Long id) {
        reviewCommentMapper.deleteById(id);
        log.info("管理员删除评价评论: reviewCommentId={}", id);
        return Result.okMsg("删除成功");
    }

    @Override
    @Transactional
    public Result batchDeleteReviewComments(List<Long> ids) {
        reviewCommentMapper.deleteByIds(ids);
        log.info("管理员批量删除评价评论: count={}", ids.size());
        return Result.okMsg("批量删除成功");
    }
}

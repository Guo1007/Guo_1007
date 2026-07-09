package gcy.system.service.Impl;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.ReviewComment;
import gcy.system.entity.vo.ReviewCommentVO;
import gcy.system.exception.BusinessException;
import gcy.system.mapper.ReviewCommentMapper;
import gcy.system.service.IReviewCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewCommentServiceImpl implements IReviewCommentService {

    private final ReviewCommentMapper reviewCommentMapper;

    @Override
    public Result getCommentsByReviewId(Long reviewId, Long userId) {
        List<ReviewCommentVO> allComments = reviewCommentMapper.selectByReviewId(reviewId, userId);
        List<ReviewCommentVO> tree = buildCommentTree(allComments);
        return Result.ok(tree);
    }

    @Override
    @Transactional
    public Result addComment(ReviewComment comment, Long userId) {
        if (comment.getReviewId() == null) {
            throw new BusinessException("评论目标不存在");
        }
        comment.setUserId(userId);
        comment.setStatus(0); // 待审核
        comment.setCreateTime(LocalDateTime.now());
        reviewCommentMapper.insert(comment);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result deleteComment(Long commentId, Long userId) {
        ReviewComment comment = reviewCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的评论");
        }
        reviewCommentMapper.deleteById(commentId);
        return Result.ok();
    }

    private List<ReviewCommentVO> buildCommentTree(List<ReviewCommentVO> allComments) {
        Map<Long, List<ReviewCommentVO>> childrenMap = allComments.stream()
                .filter(c -> c.getReplyToCommentId() != null)
                .collect(Collectors.groupingBy(ReviewCommentVO::getReplyToCommentId));
        List<ReviewCommentVO> roots = new ArrayList<>();
        for (ReviewCommentVO c : allComments) {
            if (c.getReplyToCommentId() == null) {
                c.setChildren(childrenMap.getOrDefault(c.getId(), new ArrayList<>()));
                roots.add(c);
            }
        }
        return roots;
    }
}

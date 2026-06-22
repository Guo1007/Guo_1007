package gcy.system.service;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.ReviewComment;

public interface IReviewCommentService {

    Result getCommentsByReviewId(Long reviewId, Long userId);

    Result addComment(ReviewComment comment, Long userId);

    Result deleteComment(Long commentId, Long userId);
}

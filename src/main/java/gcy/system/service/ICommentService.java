package gcy.system.service;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.CommentAppend;
import gcy.system.entity.pojo.GoodsComment;

public interface ICommentService {

    Result getCommentsByGoodsId(Long goodsId, Long userId, Integer current, Integer size);

    Result getCommentsByOrderId(Long orderId, Long userId);

    Result addComment(GoodsComment comment, Long userId);

    Result appendComment(CommentAppend append, Long userId);

    Result deleteComment(Long commentId, Long userId);

    Result deleteAppend(Long appendId, Long userId);

    Result deleteReview(Long reviewId, Long userId);
}

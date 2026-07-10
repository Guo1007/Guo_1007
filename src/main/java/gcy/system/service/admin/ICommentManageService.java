package gcy.system.service.admin;

import gcy.system.entity.dto.Result;

public interface ICommentManageService {

    Result getAllComments(Integer current, Integer size);

    Result approveComment(Long commentId);

    Result rejectComment(Long commentId);

    Result getAllAppends(Integer current, Integer size);

    Result approveAppend(Long appendId);

    Result rejectAppend(Long appendId);

    Result getAllReviewComments(Integer current, Integer size);

    Result approveReviewComment(Long commentId);

    Result rejectReviewComment(Long commentId);

    Result deleteComment(Long id);

    Result batchDeleteComments(java.util.List<Long> ids);

    Result deleteAppend(Long id);

    Result batchDeleteAppends(java.util.List<Long> ids);

    Result deleteReviewComment(Long id);

    Result batchDeleteReviewComments(java.util.List<Long> ids);
}

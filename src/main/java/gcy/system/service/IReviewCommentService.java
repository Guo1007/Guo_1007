package gcy.system.service;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.ReviewComment;

/**
 * 审核评论服务接口，提供审核评论的查询、新增与删除等业务操作。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface IReviewCommentService {

    /**
     * 根据审核记录ID获取该审核下的所有评论。
     *
     * @param reviewId 审核记录的唯一标识ID，用于定位具体的审核条目
     * @param userId   当前操作用户的唯一标识ID，用于权限校验与数据隔离
     * @return 包含该审核下评论列表的操作结果对象
     */
    Result getCommentsByReviewId(Long reviewId, Long userId);

    /**
     * 为指定审核记录新增一条评论。
     *
     * @param comment 待新增的评论实体对象，包含评论内容和关联的审核信息
     * @param userId  当前操作用户的唯一标识ID，用于记录评论人及权限校验
     * @return 包含新增评论结果的操作结果对象
     */
    Result addComment(ReviewComment comment, Long userId);

    /**
     * 根据评论ID删除指定的评论记录。
     *
     * @param commentId 待删除评论的唯一标识ID
     * @param userId    当前操作用户的唯一标识ID，用于校验当前用户是否有删除该评论的权限
     * @return 包含删除操作结果的操作结果对象
     */
    Result deleteComment(Long commentId, Long userId);
}

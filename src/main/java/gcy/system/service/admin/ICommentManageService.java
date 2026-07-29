package gcy.system.service.admin;

import gcy.system.entity.dto.Result;

/**
 * 评论管理服务接口，提供评论、追评及审核评论的后台管理功能，
 * 包括分页查询、审核通过/驳回、单条删除与批量删除、以及待处理数量统计等操作。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface ICommentManageService {

    /**
     * 分页获取所有评论列表
     *
     * @param current 当前页码
     * @param size    每页显示条数
     * @return 包含分页评论数据的统一返回结果
     */
    Result getAllComments(Integer current, Integer size);

    /**
     * 审核通过指定评论
     *
     * @param commentId 待审核通过的评论ID
     * @return 操作结果
     */
    Result approveComment(Long commentId);

    /**
     * 驳回指定评论
     *
     * @param commentId 待驳回的评论ID
     * @return 操作结果
     */
    Result rejectComment(Long commentId);

    /**
     * 分页获取所有追评列表
     *
     * @param current 当前页码
     * @param size    每页显示条数
     * @return 包含分页追评数据的统一返回结果
     */
    Result getAllAppends(Integer current, Integer size);

    /**
     * 审核通过指定追评
     *
     * @param appendId 待审核通过的追评ID
     * @return 操作结果
     */
    Result approveAppend(Long appendId);

    /**
     * 驳回指定追评
     *
     * @param appendId 待驳回的追评ID
     * @return 操作结果
     */
    Result rejectAppend(Long appendId);

    /**
     * 分页获取所有审核评论列表
     *
     * @param current 当前页码
     * @param size    每页显示条数
     * @return 包含分页审核评论数据的统一返回结果
     */
    Result getAllReviewComments(Integer current, Integer size);

    /**
     * 审核通过指定审核评论
     *
     * @param commentId 待审核通过的审核评论ID
     * @return 操作结果
     */
    Result approveReviewComment(Long commentId);

    /**
     * 驳回指定审核评论
     *
     * @param commentId 待驳回的审核评论ID
     * @return 操作结果
     */
    Result rejectReviewComment(Long commentId);

    /**
     * 根据ID删除指定评论
     *
     * @param id 待删除的评论ID
     * @return 操作结果
     */
    Result deleteComment(Long id);

    /**
     * 批量删除评论
     *
     * @param ids 待删除的评论ID列表
     * @return 操作结果
     */
    Result batchDeleteComments(java.util.List<Long> ids);

    /**
     * 根据ID删除指定追评
     *
     * @param id 待删除的追评ID
     * @return 操作结果
     */
    Result deleteAppend(Long id);

    /**
     * 批量删除追评
     *
     * @param ids 待删除的追评ID列表
     * @return 操作结果
     */
    Result batchDeleteAppends(java.util.List<Long> ids);

    /**
     * 根据ID删除指定审核评论
     *
     * @param id 待删除的审核评论ID
     * @return 操作结果
     */
    Result deleteReviewComment(Long id);

    /**
     * 批量删除审核评论
     *
     * @param ids 待删除的审核评论ID列表
     * @return 操作结果
     */
    Result batchDeleteReviewComments(java.util.List<Long> ids);

    /**
     * 获取待处理的评论、追评及审核评论的总数量
     *
     * @return 包含待处理数量的统一返回结果
     */
    Result getPendingCount();
}

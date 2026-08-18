package gcy.system.service.admin;

import gcy.system.entity.dto.Result;

import java.util.List;

/**
 * 评论管理服务接口，提供评论、追评及审核评论的后台管理功能，
 * 包括分页查询、审核通过/驳回、单条删除与批量删除、以及待处理数量统计等操作。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface ICommentManageService {

    /**
     * 分页获取评论列表
     *
     * @param current 当前页码
     * @param size    每页显示条数
     * @param statuses 状态筛选，逗号分隔（如"0,3"），为null时查全部
     * @return 包含分页评论数据的统一返回结果
     */
    Result getAllComments(Integer current, Integer size, String statuses);

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
     * @param rejectReason 拒绝原因
     * @return 操作结果
     */
    Result rejectComment(Long commentId, String rejectReason);

    /**
     * 分页获取追评列表
     *
     * @param current 当前页码
     * @param size    每页显示条数
     * @param statuses 状态筛选，逗号分隔，为null时查全部
     * @return 包含分页追评数据的统一返回结果
     */
    Result getAllAppends(Integer current, Integer size, String statuses);

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
     * @param rejectReason 拒绝原因
     * @return 操作结果
     */
    Result rejectAppend(Long appendId, String rejectReason);

    /**
     * 分页获取评价评论列表
     *
     * @param current 当前页码
     * @param size    每页显示条数
     * @param statuses 状态筛选，逗号分隔，为null时查全部
     * @return 包含分页审核评论数据的统一返回结果
     */
    Result getAllReviewComments(Integer current, Integer size, String statuses);

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
     * @param rejectReason 拒绝原因
     * @return 操作结果
     */
    Result rejectReviewComment(Long commentId, String rejectReason);

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

    /**
     * 获取各状态数量统计，用于管理端 Tab 计数。
     * <p>
     * 返回 Map 结构：
     * <ul>
     *   <li>comment: { all, pending, approved, rejected }</li>
     *   <li>append: { all, pending, approved, rejected }</li>
     *   <li>reviewComment: { all, pending, approved, rejected }</li>
     * </ul>
     * </p>
     *
     * @return 包含各状态数量的统一返回结果
     */
    Result getStatusCounts();
}

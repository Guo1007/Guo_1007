package gcy.system.service.admin;

import gcy.system.entity.dto.Result;

/**
 * 用户资料审核服务接口。
 *
 * @author 郭名城
 * @date 2026-08-18
 */
public interface IProfileReviewService {

    /**
     * 分页获取待审核的用户资料（昵称或头像）。
     *
     * @param page   页码
     * @param size   每页条数
     * @param type   审核类型：nickname / avatar
     * @return 分页的待审核用户列表
     */
    Result getPendingList(Integer page, Integer size, String type);

    /**
     * 审核通过昵称。
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    Result approveNickname(Long userId);

    /**
     * 拒绝昵称。
     *
     * @param userId 用户ID
     * @param reason 拒绝原因
     * @return 操作结果
     */
    Result rejectNickname(Long userId, String reason);

    /**
     * 审核通过头像。
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    Result approveIcon(Long userId);

    /**
     * 拒绝头像。
     *
     * @param userId 用户ID
     * @param reason 拒绝原因
     * @return 操作结果
     */
    Result rejectIcon(Long userId, String reason);
}
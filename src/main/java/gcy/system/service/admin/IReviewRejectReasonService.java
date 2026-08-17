package gcy.system.service.admin;

import gcy.system.entity.dto.Result;

/**
 * 审核拒绝原因模板服务接口。
 *
 * @author 郭名城
 * @date 2026-08-17
 */
public interface IReviewRejectReasonService {

    /** 获取所有未删除的拒绝原因模板，按排序序号升序 */
    Result listAll();
}
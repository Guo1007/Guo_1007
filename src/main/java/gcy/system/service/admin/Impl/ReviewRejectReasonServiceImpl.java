package gcy.system.service.admin.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.ReviewRejectReason;
import gcy.system.mapper.ReviewRejectReasonMapper;
import gcy.system.service.admin.IReviewRejectReasonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 审核拒绝原因模板服务实现类。
 *
 * @author 郭名城
 * @date 2026-08-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewRejectReasonServiceImpl implements IReviewRejectReasonService {

    private final ReviewRejectReasonMapper reviewRejectReasonMapper;

    @Override
    public Result listAll() {
        return Result.ok(reviewRejectReasonMapper.selectList(
                new LambdaQueryWrapper<ReviewRejectReason>()
                        .orderByAsc(ReviewRejectReason::getSortOrder)));
    }
}
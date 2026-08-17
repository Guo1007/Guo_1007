package gcy.system.controller.admin;

import gcy.system.entity.dto.Result;
import gcy.system.service.admin.IReviewRejectReasonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 审核拒绝原因模板查询控制器。
 *
 * @author 郭名城
 * @date 2026-08-17
 */
@Tag(name = "拒绝原因管理", description = "审核拒绝原因模板查询接口")
@RestController
@RequestMapping("/admin/reject-reason")
@RequiredArgsConstructor
public class ReviewRejectReasonController {

    private final IReviewRejectReasonService reviewRejectReasonService;

    @Operation(summary = "获取所有拒绝原因模板")
    @GetMapping("/list")
    public Result listAll() {
        return reviewRejectReasonService.listAll();
    }
}
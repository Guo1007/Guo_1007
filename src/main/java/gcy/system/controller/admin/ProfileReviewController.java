package gcy.system.controller.admin;

import gcy.system.aspect.OperationLog;
import gcy.system.entity.dto.Result;
import gcy.system.service.admin.IProfileReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户资料审核控制器。
 * <p>
 * 提供管理端对用户昵称和头像的审核功能，包括待审列表查询和通过/拒绝操作。
 * 基础路径：/admin/profile-review
 * </p>
 *
 * @author 郭名城
 * @date 2026-08-18
 */
@Tag(name = "用户资料审核", description = "用户昵称和头像审核接口")
@RestController
@RequestMapping("/admin/profile-review")
@RequiredArgsConstructor
public class ProfileReviewController {

    private final IProfileReviewService profileReviewService;

    /**
     * 分页获取待审核的用户资料列表。
     *
     * @param page 页码
     * @param size 每页条数
     * @param type 审核类型：nickname（昵称）/ avatar（头像），不传则查全部
     * @return 分页的待审核列表
     */
    @Operation(summary = "分页获取待审核用户资料列表")
    @GetMapping("/list")
    public Result list(@Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
                       @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
                       @Parameter(description = "审核类型") @RequestParam(required = false) String type) {
        return profileReviewService.getPendingList(page, size, type);
    }

    /**
     * 审核通过昵称。
     */
    @OperationLog("审核通过昵称")
    @Operation(summary = "审核通过昵称")
    @PutMapping("/nickname/approve/{userId}")
    public Result approveNickname(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return profileReviewService.approveNickname(userId);
    }

    /**
     * 拒绝昵称修改。
     */
    @OperationLog("拒绝昵称修改")
    @Operation(summary = "拒绝昵称修改")
    @PutMapping("/nickname/reject/{userId}")
    public Result rejectNickname(@Parameter(description = "用户ID") @PathVariable Long userId,
                                  @Parameter(description = "请求体") @RequestBody RejectRequest request) {
        return profileReviewService.rejectNickname(userId, request.getReason());
    }

    /**
     * 审核通过头像。
     */
    @OperationLog("审核通过头像")
    @Operation(summary = "审核通过头像")
    @PutMapping("/icon/approve/{userId}")
    public Result approveIcon(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return profileReviewService.approveIcon(userId);
    }

    /**
     * 拒绝头像修改。
     */
    @OperationLog("拒绝头像修改")
    @Operation(summary = "拒绝头像修改")
    @PutMapping("/icon/reject/{userId}")
    public Result rejectIcon(@Parameter(description = "用户ID") @PathVariable Long userId,
                              @Parameter(description = "请求体") @RequestBody RejectRequest request) {
        return profileReviewService.rejectIcon(userId, request.getReason());
    }

    @Data
    public static class RejectRequest {
        private String reason;
    }
}
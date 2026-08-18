package gcy.system.controller.admin;

import gcy.system.aspect.OperationLog;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.SendNotificationFormDTO;
import gcy.system.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知管理控制器
 * <p>
 * 提供通知的发送、分页查询、更新、删除和批量删除等后台管理接口，
 * 所有接口均挂载在 /admin/notification 路径下。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Tag(name = "通知管理", description = "通知管理相关接口")
@RestController
@RequestMapping("/admin/notification")
@RequiredArgsConstructor
public class NotificationManageController {

    private final INotificationService notificationService;

    /**
     * 发送通知
     * <p>
     * POST /admin/notification/send —— 向指定用户或用户组发送一条通知消息。
     *
     * @param dto 发送通知的表单数据，包含通知标题、内容、接收者等信息
     * @return 操作结果，封装在 {@link Result} 中
     */
    @OperationLog("发送通知")
    @Operation(summary = "发送通知")
    @PostMapping("/send")
    public Result sendNotification(@Parameter(description = "请求体") @RequestBody @Valid SendNotificationFormDTO dto) {
        return notificationService.sendNotification(dto);
    }

    /**
     * 分页查询通知列表
     * <p>
     * GET /admin/notification/list —— 按分页参数和可选类型筛选条件查询通知列表。
     *
     * @param current 当前页码，从 1 开始，默认为 1
     * @param size    每页显示的记录数，默认为 10
     * @param type    通知类型（可选），传入时按该类型过滤，不传则查询全部类型
     * @return 分页后的通知列表及分页信息，封装在 {@link Result} 中
     */
    @Operation(summary = "分页查询通知列表")
    @GetMapping("/list")
    public Result list(@Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
                       @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
                       @Parameter(description = "通知类型") @RequestParam(required = false) String type) {
        return notificationService.getAllNotifications(current, size, type);
    }

    /**
     * 更新通知
     * <p>
     * PUT /admin/notification/update/{id} —— 根据通知 ID 修改已存在的通知内容。
     *
     * @param id  要更新的通知 ID（路径变量）
     * @param dto 更新后的通知表单数据，包含修改后的标题、内容等信息
     * @return 操作结果，封装在 {@link Result} 中
     */
    @OperationLog("更新通知")
    @Operation(summary = "更新通知")
    @PutMapping("/update/{id}")
    public Result update(@Parameter(description = "通知ID") @PathVariable Long id, @Parameter(description = "请求体") @RequestBody @Valid SendNotificationFormDTO dto) {
        return notificationService.updateNotification(id, dto);
    }

    /**
     * 删除单条通知
     * <p>
     * DELETE /admin/notification/delete/{id} —— 根据通知 ID 删除指定通知。
     *
     * @param id 要删除的通知 ID（路径变量）
     * @return 操作结果，封装在 {@link Result} 中
     */
    @OperationLog("删除通知")
    @Operation(summary = "删除单条通知")
    @DeleteMapping("/delete/{id}")
    public Result delete(@Parameter(description = "通知ID") @PathVariable Long id) {
        return notificationService.deleteNotification(id);
    }

    /**
     * 批量删除通知
     * <p>
     * DELETE /admin/notification/batch —— 根据请求体中传入的 ID 列表批量删除通知。
     *
     * @param ids 待删除的通知 ID 列表，以 JSON 数组形式放在请求体中
     * @return 操作结果，成功时返回成功提示信息，失败时返回失败提示信息，封装在 {@link Result} 中
     */
    @OperationLog("批量删除通知")
    @Operation(summary = "批量删除通知")
    @DeleteMapping("/batch")
    public Result batchDelete(@Parameter(description = "通知ID列表") @RequestBody List<Long> ids) {
        boolean success = notificationService.removeByIds(ids);
        return success ? Result.okMsg("批量删除成功") : Result.fail("批量删除失败");
    }
}

package gcy.system.controller;

import gcy.system.entity.dto.Result;
import gcy.system.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 通知管理控制器
 * <p>
 * 提供通知相关的REST API接口，包括通知列表查询、未读数量统计、
 * 标记已读（单个/全部）以及删除通知等功能。
 * 所有接口均基于当前登录用户进行操作。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Tag(name = "通知", description = "通知相关接口")
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;

    /**
     * 分页查询当前用户的通知列表
     *
     * @param current 当前页码，默认值为1
     * @param size    每页显示条数，默认值为10
     * @return 包含分页通知数据的统一响应结果
     */
    @Operation(summary = "分页查询当前用户的通知列表")
    @GetMapping("/list")
    public Result list(@Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
                       @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size) {
        return notificationService.getUserNotifications(current, size);
    }

    /**
     * 获取当前用户的未读通知数量
     *
     * @return 包含未读数量的统一响应结果
     */
    @Operation(summary = "获取当前用户的未读通知数量")
    @GetMapping("/unread-count")
    public Result unreadCount() {
        return notificationService.getUnreadCount();
    }

    /**
     * 将指定通知标记为已读
     *
     * @param id 要标记为已读的通知ID
     * @return 操作结果的统一响应
     */
    @Operation(summary = "将指定通知标记为已读")
    @PutMapping("/read/{id}")
    public Result markRead(@Parameter(description = "通知ID") @PathVariable Long id) {
        return notificationService.markAsRead(id);
    }

    /**
     * 将当前用户的所有未读通知标记为已读
     *
     * @return 操作结果的统一响应
     */
    @Operation(summary = "将当前用户的所有未读通知标记为已读")
    @PutMapping("/read-all")
    public Result markAllRead() {
        return notificationService.markAllAsRead();
    }

    /**
     * 删除当前用户的一条通知记录
     *
     * @param id 要删除的通知ID
     * @return 操作结果的统一响应
     */
    @Operation(summary = "删除当前用户的一条通知记录")
    @DeleteMapping("/{id}")
    public Result deleteMyNotification(@Parameter(description = "通知ID") @PathVariable Long id) {
        return notificationService.deleteMyNotification(id);
    }
}

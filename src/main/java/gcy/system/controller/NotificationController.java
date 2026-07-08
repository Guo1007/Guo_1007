package gcy.system.controller;

import gcy.system.entity.dto.Result;
import gcy.system.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size) {
        return notificationService.getUserNotifications(current, size);
    }

    @GetMapping("/unread-count")
    public Result unreadCount() {
        return notificationService.getUnreadCount();
    }

    @PutMapping("/read/{id}")
    public Result markRead(@PathVariable Long id) {
        return notificationService.markAsRead(id);
    }

    @PutMapping("/read-all")
    public Result markAllRead() {
        return notificationService.markAllAsRead();
    }

    @DeleteMapping("/{id}")
    public Result deleteMyNotification(@PathVariable Long id) {
        return notificationService.deleteMyNotification(id);
    }
}

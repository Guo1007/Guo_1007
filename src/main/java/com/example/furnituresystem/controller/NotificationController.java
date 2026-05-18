package com.example.furnituresystem.controller;

import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.service.INotificationService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Resource
    private INotificationService notificationService;

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
}

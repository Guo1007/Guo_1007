package com.example.furnituresystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.furnituresystem.entity.dto.NotificationVO;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.dto.SendNotificationFormDTO;

public interface INotificationService {

    /**
     * 管理员发送通知
     */
    Result sendNotification(SendNotificationFormDTO dto);

    /**
     * 获取当前用户的通知列表
     */
    Result getUserNotifications(Integer current, Integer size);

    /**
     * 获取当前用户未读数
     */
    Result getUnreadCount();

    /**
     * 标记单条已读
     */
    Result markAsRead(Long notificationId);

    /**
     * 标记全部已读
     */
    Result markAllAsRead();

    /**
     * 管理员获取所有通知列表
     */
    Result getAllNotifications(Integer current, Integer size, String type);

    /**
     * 管理员更新通知
     */
    Result updateNotification(Long id, SendNotificationFormDTO dto);

    /**
     * 管理员删除通知
     */
    Result deleteNotification(Long id);
}

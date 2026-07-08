package gcy.system.service;

import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.SendNotificationFormDTO;

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

    /**
     * 用户删除自己的通知（仅删除自己视角，不影响其他人）
     */
    Result deleteMyNotification(Long notificationId);
}

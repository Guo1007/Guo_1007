package gcy.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.SendNotificationFormDTO;
import gcy.system.entity.pojo.Notification;

/**
 * 通知服务接口，定义通知的发送、查询、已读标记及管理相关的业务操作。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface INotificationService extends IService<Notification> {

    /**
     * 管理员发送通知，将通知推送给目标用户群体。
     *
     * @param dto 发送通知的表单数据，包含通知标题、内容、类型及目标用户范围等信息
     * @return 操作结果，包含发送是否成功及对应的状态信息
     */
    Result sendNotification(SendNotificationFormDTO dto);

    /**
     * 获取当前登录用户的通知列表，支持分页查询。
     *
     * @param current 当前页码，用于分页查询
     * @param size    每页显示的记录数量
     * @return 操作结果，包含分页后的通知列表数据及分页信息
     */
    Result getUserNotifications(Integer current, Integer size);

    /**
     * 获取当前登录用户的未读通知数量。
     *
     * @return 操作结果，包含当前用户的未读通知数量
     */
    Result getUnreadCount();

    /**
     * 将指定通知标记为已读状态。
     *
     * @param notificationId 要标记为已读的通知ID
     * @return 操作结果，包含标记操作是否成功
     */
    Result markAsRead(Long notificationId);

    /**
     * 将当前登录用户的所有未读通知标记为已读状态。
     *
     * @return 操作结果，包含批量标记操作是否成功
     */
    Result markAllAsRead();

    /**
     * 管理员获取系统中所有通知列表，支持按类型筛选和分页查询。
     *
     * @param current 当前页码，用于分页查询
     * @param size    每页显示的记录数量
     * @param type    通知类型筛选条件，为空时查询全部类型
     * @return 操作结果，包含分页后的通知列表数据及分页信息
     */
    Result getAllNotifications(Integer current, Integer size, String type);

    /**
     * 管理员更新指定通知的内容。
     *
     * @param id  要更新的通知ID
     * @param dto 更新后的通知表单数据，包含新的标题、内容、类型等信息
     * @return 操作结果，包含更新是否成功及对应的状态信息
     */
    Result updateNotification(Long id, SendNotificationFormDTO dto);

    /**
     * 管理员删除指定通知，该操作会从系统中彻底移除通知记录。
     *
     * @param id 要删除的通知ID
     * @return 操作结果，包含删除是否成功及对应的状态信息
     */
    Result deleteNotification(Long id);

    /**
     * 用户删除自己视角下的通知记录，该操作仅影响当前用户的通知列表，不影响其他用户的数据。
     *
     * @param notificationId 要删除的通知ID
     * @return 操作结果，包含删除是否成功及对应的状态信息
     */
    Result deleteMyNotification(Long notificationId);
}

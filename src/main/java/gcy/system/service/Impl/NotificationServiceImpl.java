package gcy.system.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.RocketMQMessage;
import gcy.system.entity.dto.SendNotificationFormDTO;
import gcy.system.entity.dto.UserDTO;
import gcy.system.entity.pojo.Notification;
import gcy.system.entity.pojo.User;
import gcy.system.entity.pojo.UserNotification;
import gcy.system.entity.vo.NotificationVO;
import gcy.system.mapper.NotificationMapper;
import gcy.system.mapper.UserMapper;
import gcy.system.mapper.UserNotificationMapper;
import gcy.system.service.EmailService;
import gcy.system.service.INotificationService;
import gcy.system.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements INotificationService {

    private final UserMapper userMapper;

    private final UserNotificationMapper userNotificationMapper;

    private final RocketMQTemplate rocketMQTemplate;

    private final EmailService emailService;

    @Override
    @Transactional
    public Result sendNotification(SendNotificationFormDTO dto) {
        Notification notification = new Notification();
        notification.setUserId(dto.getUserId());
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getContent());
        notification.setType(dto.getType() != null ? dto.getType() : "system");
        notification.setCreateTime(LocalDateTime.now());
        save(notification);

        if (Boolean.TRUE.equals(dto.getSendEmail())) {
            if (dto.getUserId() != null) {
                User target = userMapper.selectById(dto.getUserId());
                if (target == null) {
                    return Result.ok("通知已保存，但目标用户不存在，邮件未发送");
                }
                if (StrUtil.isBlank(target.getEmail())) {
                    return Result.ok("通知已保存，但该用户（" + target.getUserName() + "）未绑定邮箱，邮件未发送");
                }
                sendNotificationMq(target, dto.getTitle(), dto.getContent());
            } else {
                List<User> allUsers = userMapper.selectList(
                        new LambdaQueryWrapper<User>().isNotNull(User::getEmail).ne(User::getEmail, ""));
                if (allUsers.isEmpty()) {
                    return Result.ok("通知已保存，但系统中没有已绑定邮箱的用户，邮件未发送");
                }
                // 全体通知只发 1 条 MQ，由消费者负责群发，避免请求线程阻塞
                try {
                    RocketMQMessage msg = new RocketMQMessage();
                    msg.setType("notification-all");
                    msg.setTitle(dto.getTitle());
                    msg.setContent(dto.getContent());
                    rocketMQTemplate.convertAndSend("notification-email-topic", JSONUtil.toJsonStr(msg));
                    log.info("全体通知MQ已发送，覆盖 {} 位用户", allUsers.size());
                } catch (Exception e) {
                    log.error("MQ发送失败，回退到直接邮件群发", e);
                    for (User u : allUsers) {
                        emailService.sendNotificationEmail(u.getEmail(), dto.getTitle(), dto.getContent());
                    }
                }
                return Result.ok("通知已保存，已向 " + allUsers.size() + " 位用户发送邮件通知");
            }
        }
        return Result.ok("发送成功");
    }

    @Override
    public Result getUserNotifications(Integer current, Integer size) {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 查询用户删除的通知ID集合
        Set<Long> deletedIds = getDeletedNotificationIds(userId);

        Page<Notification> page = new Page<>(current, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Notification::getUserId, userId)
                .or().isNull(Notification::getUserId));
        // 排除用户已删除的通知
        if (!deletedIds.isEmpty()) {
            wrapper.notIn(Notification::getId, deletedIds);
        }
        wrapper.orderByDesc(Notification::getCreateTime);
        Page<Notification> result = page(page, wrapper);

        // 查询当前用户已读的通知ID集合
        List<Long> readNotificationIds = getReadNotificationIds(userId, result.getRecords());

        List<NotificationVO> voList = result.getRecords().stream()
                .map(n -> {
                    NotificationVO vo = BeanUtil.copyProperties(n, NotificationVO.class);
                    vo.setIsRead(readNotificationIds.contains(n.getId()));
                    return vo;
                })
                .collect(Collectors.toList());

        Page<NotificationVO> voPage = new Page<>();
        BeanUtil.copyProperties(result, voPage, "records");
        voPage.setRecords(voList);
        return Result.ok(voPage);
    }

    /**
     * 查询当前页通知中用户已读的通知ID
     */
    private List<Long> getReadNotificationIds(Long userId, List<Notification> notifications) {
        if (notifications.isEmpty()) {
            return List.of();
        }
        List<Long> notificationIds = notifications.stream()
                .map(Notification::getId)
                .collect(Collectors.toList());
        LambdaQueryWrapper<UserNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotification::getUserId, userId)
                .eq(UserNotification::getIsRead, 1)
                .eq(UserNotification::getIsDeleted, 0)
                .in(UserNotification::getNotificationId, notificationIds);
        return userNotificationMapper.selectList(wrapper).stream()
                .map(UserNotification::getNotificationId)
                .collect(Collectors.toList());
    }

    /**
     * 查询用户已删除的通知ID集合（全量，用于排除）
     */
    private Set<Long> getDeletedNotificationIds(Long userId) {
        LambdaQueryWrapper<UserNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotification::getUserId, userId)
                .eq(UserNotification::getIsDeleted, 1)
                .select(UserNotification::getNotificationId);
        return userNotificationMapper.selectList(wrapper).stream()
                .map(UserNotification::getNotificationId)
                .collect(Collectors.toSet());
    }

    @Override
    public Result getUnreadCount() {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 查询用户已删除的通知ID
        Set<Long> deletedIds = getDeletedNotificationIds(userId);

        // 查询用户可见的所有通知ID
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Notification::getUserId, userId)
                .or().isNull(Notification::getUserId));
        if (!deletedIds.isEmpty()) {
            wrapper.notIn(Notification::getId, deletedIds);
        }
        wrapper.select(Notification::getId);
        List<Long> allNotificationIds = list(wrapper).stream()
                .map(Notification::getId)
                .collect(Collectors.toList());

        if (allNotificationIds.isEmpty()) {
            return Result.ok(0L);
        }

        // 查询用户已读的通知ID
        LambdaQueryWrapper<UserNotification> readWrapper = new LambdaQueryWrapper<>();
        readWrapper.eq(UserNotification::getUserId, userId)
                .eq(UserNotification::getIsRead, 1)
                .eq(UserNotification::getIsDeleted, 0)
                .in(UserNotification::getNotificationId, allNotificationIds);
        long readCount = userNotificationMapper.selectCount(readWrapper);

        return Result.ok(allNotificationIds.size() - readCount);
    }

    @Override
    @Transactional
    public Result markAsRead(Long notificationId) {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        Notification notification = getById(notificationId);
        if (notification == null) {
            return Result.fail("通知不存在");
        }

        // 校验通知归属：仅允许标记自己可见的通知为已读
        if (notification.getUserId() != null && !notification.getUserId().equals(userId)) {
            return Result.fail("无权操作该通知");
        }

        // upsert：有记录则更新，无记录则插入
        upsertUserNotification(userId, notificationId, true, false);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result markAllAsRead() {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 查询用户已删除的通知ID
        Set<Long> deletedIds = getDeletedNotificationIds(userId);

        // 查询用户可见的所有通知
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Notification::getUserId, userId)
                .or().isNull(Notification::getUserId));
        if (!deletedIds.isEmpty()) {
            wrapper.notIn(Notification::getId, deletedIds);
        }
        wrapper.select(Notification::getId);
        List<Long> allNotificationIds = list(wrapper).stream()
                .map(Notification::getId)
                .collect(Collectors.toList());

        if (allNotificationIds.isEmpty()) {
            return Result.ok();
        }

        // 查询已读的通知ID
        LambdaQueryWrapper<UserNotification> readWrapper = new LambdaQueryWrapper<>();
        readWrapper.eq(UserNotification::getUserId, userId)
                .eq(UserNotification::getIsRead, 1)
                .eq(UserNotification::getIsDeleted, 0)
                .in(UserNotification::getNotificationId, allNotificationIds);
        Set<Long> readIds = userNotificationMapper.selectList(readWrapper).stream()
                .map(UserNotification::getNotificationId)
                .collect(Collectors.toSet());

        // 批量标记未读通知为已读（upsert）
        LocalDateTime now = LocalDateTime.now();
        for (Long id : allNotificationIds) {
            if (!readIds.contains(id)) {
                upsertUserNotification(userId, id, true, false);
            }
        }
        return Result.ok();
    }

    @Override
    @Transactional
    public Result deleteMyNotification(Long notificationId) {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        Notification notification = getById(notificationId);
        if (notification == null) {
            return Result.fail("通知不存在");
        }

        // 校验通知归属
        if (notification.getUserId() != null && !notification.getUserId().equals(userId)) {
            return Result.fail("无权操作该通知");
        }

        // upsert 为已删除状态
        upsertUserNotification(userId, notificationId, null, true);
        return Result.ok("已删除");
    }

    /**
     * 插入或更新用户通知状态（已读/未读/删除）。
     * 利用 user_notification 的 uk_notification_user 唯一索引做 upsert。
     */
    private void upsertUserNotification(Long userId, Long notificationId, Boolean isRead, Boolean isDeleted) {
        // 先查是否存在
        LambdaQueryWrapper<UserNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotification::getUserId, userId)
                .eq(UserNotification::getNotificationId, notificationId);
        UserNotification existing = userNotificationMapper.selectOne(wrapper);

        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            // 更新已有记录
            LambdaUpdateWrapper<UserNotification> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserNotification::getId, existing.getId());
            if (isRead != null) {
                updateWrapper.set(UserNotification::getIsRead, isRead ? 1 : 0);
                if (isRead) {
                    updateWrapper.set(UserNotification::getReadTime, now);
                }
            }
            if (isDeleted != null) {
                updateWrapper.set(UserNotification::getIsDeleted, isDeleted ? 1 : 0);
            }
            updateWrapper.set(UserNotification::getUpdateTime, now);
            userNotificationMapper.update(null, updateWrapper);
        } else {
            // 插入新记录
            UserNotification un = new UserNotification();
            un.setUserId(userId);
            un.setNotificationId(notificationId);
            un.setIsRead(isRead != null && isRead ? 1 : 0);
            un.setIsDeleted(isDeleted != null && isDeleted ? 1 : 0);
            un.setReadTime(isRead != null && isRead ? now : null);
            un.setUpdateTime(now);
            userNotificationMapper.insert(un);
        }
    }

    @Override
    public Result getAllNotifications(Integer current, Integer size, String type) {
        Page<Notification> page = new Page<>(current, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Notification::getCreateTime);
        if (StrUtil.isNotBlank(type)) {
            wrapper.eq(Notification::getType, type);
        }
        Page<Notification> result = page(page, wrapper);

        List<Long> targetUserIds = result.getRecords().stream()
                .map(Notification::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> userNameMap = Map.of();
        if (!targetUserIds.isEmpty()) {
            List<User> users = userMapper.selectByIds(targetUserIds);
            userNameMap = users.stream()
                    .collect(Collectors.toMap(User::getId, User::getUserName, (a, b) -> a));
        }

        Map<Long, String> finalUserNameMap = userNameMap;
        List<NotificationVO> voList = result.getRecords().stream()
                .map(n -> {
                    NotificationVO vo = BeanUtil.copyProperties(n, NotificationVO.class);
                    vo.setUserId(n.getUserId());
                    if (n.getUserId() != null) {
                        vo.setUserName(finalUserNameMap.getOrDefault(n.getUserId(), "未知用户"));
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        Page<NotificationVO> voPage = new Page<>();
        BeanUtil.copyProperties(result, voPage, "records");
        voPage.setRecords(voList);
        return Result.ok(voPage);
    }

    @Override
    @Transactional
    public Result updateNotification(Long id, SendNotificationFormDTO dto) {
        Notification notification = getById(id);
        if (notification == null) {
            return Result.fail("通知不存在");
        }
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getContent());
        notification.setType(dto.getType() != null ? dto.getType() : "system");
        notification.setUserId(dto.getUserId());
        updateById(notification);
        return Result.ok("修改成功");
    }

    @Override
    @Transactional
    public Result deleteNotification(Long id) {
        Notification notification = getById(id);
        if (notification == null) {
            return Result.fail("通知不存在");
        }
        removeById(id);
        return Result.ok("删除成功");
    }

    private void sendNotificationMq(User user, String title, String content) {
        try {
            RocketMQMessage msg = new RocketMQMessage();
            msg.setType("notification");
            msg.setUserId(user.getId());
            msg.setUserEmail(user.getEmail());
            msg.setUserName(user.getUserName());
            msg.setTitle(title);
            msg.setContent(content);
            rocketMQTemplate.convertAndSend("notification-email-topic", JSONUtil.toJsonStr(msg));
            log.info("通知MQ消息已发送: userId={}, email={}", user.getId(), user.getEmail());
        } catch (Exception e) {
            log.error("MQ发送失败，回退到直接邮件发送: userId={}", user.getId(), e);
            emailService.sendNotificationEmail(user.getEmail(), title, content);
        }
    }
}

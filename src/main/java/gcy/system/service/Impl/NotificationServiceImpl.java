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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
                    return Result.okMsg("通知已保存，但目标用户不存在，邮件未发送");
                }
                if (StrUtil.isBlank(target.getEmail())) {
                    return Result.okMsg("通知已保存，但该用户（" + target.getUserName() + "）未绑定邮箱，邮件未发送");
                }
                sendNotificationMq(target, dto.getTitle(), dto.getContent());
            } else {
                List<User> allUsers = userMapper.selectList(
                        new LambdaQueryWrapper<User>().isNotNull(User::getEmail).ne(User::getEmail, ""));
                if (allUsers.isEmpty()) {
                    return Result.okMsg("通知已保存，但系统中没有已绑定邮箱的用户，邮件未发送");
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
                return Result.okMsg("通知已保存，已向 " + allUsers.size() + " 位用户发送邮件通知");
            }
        }
        return Result.okMsg("发送成功");
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

        // 批量标记未读通知为已读
        List<Long> unreadIds = allNotificationIds.stream()
                .filter(id -> !readIds.contains(id))
                .collect(Collectors.toList());

        if (unreadIds.isEmpty()) {
            return Result.ok();
        }

        LocalDateTime now = LocalDateTime.now();

        // 一次查询获取所有已有记录，避免循环内逐条 selectOne（N+1）
        LambdaQueryWrapper<UserNotification> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(UserNotification::getUserId, userId)
                .in(UserNotification::getNotificationId, unreadIds);
        Map<Long, UserNotification> existingMap = userNotificationMapper.selectList(existingWrapper).stream()
                .collect(Collectors.toMap(UserNotification::getNotificationId, un -> un, (a, b) -> a));

        // 批量更新已有记录
        List<Long> existingUnreadIds = unreadIds.stream()
                .filter(existingMap::containsKey)
                .collect(Collectors.toList());
        if (!existingUnreadIds.isEmpty()) {
            LambdaUpdateWrapper<UserNotification> batchUpdate = new LambdaUpdateWrapper<>();
            batchUpdate.eq(UserNotification::getUserId, userId)
                    .in(UserNotification::getNotificationId, existingUnreadIds)
                    .set(UserNotification::getIsRead, 1)
                    .set(UserNotification::getIsDeleted, 0)
                    .set(UserNotification::getReadTime, now)
                    .set(UserNotification::getUpdateTime, now);
            userNotificationMapper.update(null, batchUpdate);
        }

        // 批量插入新记录
        List<Long> missingIds = unreadIds.stream()
                .filter(id -> !existingMap.containsKey(id))
                .collect(Collectors.toList());
        if (!missingIds.isEmpty()) {
            List<UserNotification> batch = missingIds.stream().map(nid -> {
                UserNotification un = new UserNotification();
                un.setUserId(userId);
                un.setNotificationId(nid);
                un.setIsRead(1);
                un.setIsDeleted(0);
                un.setReadTime(now);
                un.setUpdateTime(now);
                return un;
            }).collect(Collectors.toList());

            // 逐条插入并兜底并发冲突（uk_notification_user）
            for (UserNotification un : batch) {
                try {
                    userNotificationMapper.insert(un);
                } catch (DuplicateKeyException e) {
                    log.debug("markAllAsRead 并发冲突: userId={}, notificationId={}", userId, un.getNotificationId());
                    LambdaUpdateWrapper<UserNotification> fallback = new LambdaUpdateWrapper<>();
                    fallback.eq(UserNotification::getUserId, userId)
                            .eq(UserNotification::getNotificationId, un.getNotificationId())
                            .set(UserNotification::getIsRead, 1)
                            .set(UserNotification::getReadTime, now)
                            .set(UserNotification::getUpdateTime, now);
                    userNotificationMapper.update(null, fallback);
                }
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
        return Result.okMsg("已删除");
    }

    /**
     * 插入或更新用户通知状态（已读/未读/删除）。
     * 利用 user_notification 的 uk_notification_user 唯一索引做 upsert。
     * 先查后插，插入失败（并发冲突）时回退为更新，避免 DuplicateKeyException 导致 500。
     */
    private void upsertUserNotification(Long userId, Long notificationId, Boolean isRead, Boolean isDeleted) {
        LocalDateTime now = LocalDateTime.now();

        // 先查是否存在
        LambdaQueryWrapper<UserNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotification::getUserId, userId)
                .eq(UserNotification::getNotificationId, notificationId);
        UserNotification existing = userNotificationMapper.selectOne(wrapper);

        if (existing != null) {
            // 已有记录：直接更新
            doUpdateUserNotification(existing.getId(), isRead, isDeleted, now);
            return;
        }

        // 无记录：尝试插入
        UserNotification un = new UserNotification();
        un.setUserId(userId);
        un.setNotificationId(notificationId);
        un.setIsRead(isRead != null && isRead ? 1 : 0);
        un.setIsDeleted(isDeleted != null && isDeleted ? 1 : 0);
        un.setReadTime(isRead != null && isRead ? now : null);
        un.setUpdateTime(now);

        try {
            userNotificationMapper.insert(un);
        } catch (DuplicateKeyException e) {
            // 并发下另一线程已插入，回退为查询并更新
            log.debug("upsert 并发冲突，回退为更新: userId={}, notificationId={}", userId, notificationId);
            existing = userNotificationMapper.selectOne(wrapper);
            if (existing != null) {
                doUpdateUserNotification(existing.getId(), isRead, isDeleted, now);
            }
        }
    }

    /**
     * 更新已有 user_notification 记录。
     */
    private void doUpdateUserNotification(Long id, Boolean isRead, Boolean isDeleted, LocalDateTime now) {
        LambdaUpdateWrapper<UserNotification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserNotification::getId, id);
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
        return Result.okMsg("修改成功");
    }

    @Override
    @Transactional
    public Result deleteNotification(Long id) {
        Notification notification = getById(id);
        if (notification == null) {
            return Result.fail("通知不存在");
        }
        removeById(id);
        return Result.okMsg("删除成功");
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

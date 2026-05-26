package com.example.furnituresystem.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.dto.RocketMQMessage;
import com.example.furnituresystem.entity.dto.SendNotificationFormDTO;
import com.example.furnituresystem.entity.dto.UserDTO;
import com.example.furnituresystem.entity.pojo.Notification;
import com.example.furnituresystem.entity.pojo.NotificationRead;
import com.example.furnituresystem.entity.pojo.User;
import com.example.furnituresystem.entity.vo.NotificationVO;
import com.example.furnituresystem.mapper.NotificationMapper;
import com.example.furnituresystem.mapper.NotificationReadMapper;
import com.example.furnituresystem.mapper.UserMapper;
import com.example.furnituresystem.service.EmailService;
import com.example.furnituresystem.service.INotificationService;
import com.example.furnituresystem.utils.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
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
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements INotificationService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private NotificationReadMapper notificationReadMapper;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private EmailService emailService;

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

        Page<Notification> page = new Page<>(current, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Notification::getUserId, userId)
                .or().isNull(Notification::getUserId));
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

    private List<Long> getReadNotificationIds(Long userId, List<Notification> notifications) {
        if (notifications.isEmpty()) {
            return List.of();
        }
        List<Long> notificationIds = notifications.stream()
                .map(Notification::getId)
                .collect(Collectors.toList());
        LambdaQueryWrapper<NotificationRead> readWrapper = new LambdaQueryWrapper<>();
        readWrapper.eq(NotificationRead::getUserId, userId)
                .in(NotificationRead::getNotificationId, notificationIds);
        return notificationReadMapper.selectList(readWrapper).stream()
                .map(NotificationRead::getNotificationId)
                .collect(Collectors.toList());
    }

    @Override
    public Result getUnreadCount() {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 查询用户可见的所有通知ID
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Notification::getUserId, userId)
                .or().isNull(Notification::getUserId));
        wrapper.select(Notification::getId);
        List<Long> allNotificationIds = list(wrapper).stream()
                .map(Notification::getId)
                .collect(Collectors.toList());

        if (allNotificationIds.isEmpty()) {
            return Result.ok(0L);
        }

        // 查询用户已读的通知ID
        LambdaQueryWrapper<NotificationRead> readWrapper = new LambdaQueryWrapper<>();
        readWrapper.eq(NotificationRead::getUserId, userId)
                .in(NotificationRead::getNotificationId, allNotificationIds);
        long readCount = notificationReadMapper.selectCount(readWrapper);

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

        // 检查是否已读
        LambdaQueryWrapper<NotificationRead> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(NotificationRead::getNotificationId, notificationId)
                .eq(NotificationRead::getUserId, userId);
        if (notificationReadMapper.selectCount(checkWrapper) > 0) {
            return Result.ok();
        }

        // 插入已读记录
        NotificationRead read = new NotificationRead();
        read.setNotificationId(notificationId);
        read.setUserId(userId);
        read.setReadTime(LocalDateTime.now());
        notificationReadMapper.insert(read);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result markAllAsRead() {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 查询用户可见的所有通知
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Notification::getUserId, userId)
                .or().isNull(Notification::getUserId));
        wrapper.select(Notification::getId);
        List<Long> allNotificationIds = list(wrapper).stream()
                .map(Notification::getId)
                .collect(Collectors.toList());

        if (allNotificationIds.isEmpty()) {
            return Result.ok();
        }

        // 查询已读的通知ID
        LambdaQueryWrapper<NotificationRead> readWrapper = new LambdaQueryWrapper<>();
        readWrapper.eq(NotificationRead::getUserId, userId)
                .in(NotificationRead::getNotificationId, allNotificationIds);
        Set<Long> readIds = notificationReadMapper.selectList(readWrapper).stream()
                .map(NotificationRead::getNotificationId)
                .collect(Collectors.toSet());

        // 批量插入未读通知的已读记录
        LocalDateTime now = LocalDateTime.now();
        List<NotificationRead> toInsert = allNotificationIds.stream()
                .filter(id -> !readIds.contains(id))
                .map(id -> {
                    NotificationRead read = new NotificationRead();
                    read.setNotificationId(id);
                    read.setUserId(userId);
                    read.setReadTime(now);
                    return read;
                })
                .collect(Collectors.toList());

        if (!toInsert.isEmpty()) {
            for (NotificationRead read : toInsert) {
                notificationReadMapper.insert(read);
            }
        }
        return Result.ok();
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
            List<User> users = userMapper.selectBatchIds(targetUserIds);
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

package gcy.system.service.admin.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.SendNotificationFormDTO;
import gcy.system.entity.pojo.User;
import gcy.system.mapper.UserMapper;
import gcy.system.service.INotificationService;
import gcy.system.service.admin.IProfileReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户资料审核服务实现。
 *
 * @author 郭名城
 * @date 2026-08-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileReviewServiceImpl implements IProfileReviewService {

    private final UserMapper userMapper;

    private final INotificationService notificationService;

    @Override
    public Result getNicknameList(Integer page, Integer size, String status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(User::getPendingNickname).ne(User::getPendingNickname, "");
        if (status != null && !status.isEmpty()) {
            List<Integer> statusList = Arrays.stream(status.split(","))
                    .map(Integer::parseInt).collect(Collectors.toList());
            wrapper.in(User::getNicknameReviewStatus, statusList);
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> pageResult = userMapper.selectPage(new Page<>(page, size), wrapper);
        return buildResult(pageResult, "nickname");
    }

    @Override
    public Result getIconList(Integer page, Integer size, String status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(User::getPendingIcon).ne(User::getPendingIcon, "");
        if (status != null && !status.isEmpty()) {
            List<Integer> statusList = Arrays.stream(status.split(","))
                    .map(Integer::parseInt).collect(Collectors.toList());
            wrapper.in(User::getIconReviewStatus, statusList);
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> pageResult = userMapper.selectPage(new Page<>(page, size), wrapper);
        return buildResult(pageResult, "icon");
    }

    private Result buildResult(Page<User> pageResult, String type) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (User user : pageResult.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", user.getId());
            item.put("userName", user.getUserName());
            if ("nickname".equals(type)) {
                item.put("pendingNickname", user.getPendingNickname());
                item.put("reviewStatus", user.getNicknameReviewStatus());
                item.put("aiRejectReason", user.getNicknameAiRejectReason());
                item.put("manualRejectReason", user.getNicknameManualRejectReason());
            } else {
                item.put("pendingIcon", user.getPendingIcon());
                item.put("reviewStatus", user.getIconReviewStatus());
            }
            records.add(item);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", pageResult.getTotal());
        return Result.ok(data);
    }

    @Override
    @Transactional
    public Result approveNickname(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return Result.fail("用户不存在");
        if (user.getNicknameReviewStatus() != 1 && user.getNicknameReviewStatus() != 3) {
            return Result.fail("该用户无待审核的昵称");
        }

        user.setUserName(user.getPendingNickname());
        user.setPendingNickname(null);
        user.setNicknameReviewStatus(0);
        user.setNicknameAiRejectReason(null);
        user.setNicknameManualRejectReason(null);
        userMapper.updateById(user);

        sendNotification(userId, "昵称审核通过", "您的新昵称「" + user.getUserName() + "」已通过审核");
        log.info("昵称审核通过: userId={}, nickname={}", userId, user.getUserName());
        return Result.okMsg("昵称审核通过");
    }

    @Override
    @Transactional
    public Result rejectNickname(Long userId, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) return Result.fail("用户不存在");
        if (user.getNicknameReviewStatus() != 1 && user.getNicknameReviewStatus() != 3) {
            return Result.fail("该用户无待审核的昵称");
        }

        user.setPendingNickname(null);
        user.setNicknameReviewStatus(2);
        user.setNicknameManualRejectReason(reason);
        userMapper.updateById(user);

        String msg = "您的新昵称未通过审核";
        if (reason != null && !reason.isEmpty()) {
            msg += "，原因：" + reason;
        }
        sendNotification(userId, "昵称审核未通过", msg);
        log.info("昵称审核拒绝: userId={}, reason={}", userId, reason);
        return Result.okMsg("已拒绝昵称修改");
    }

    @Override
    @Transactional
    public Result approveIcon(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return Result.fail("用户不存在");
        if (user.getIconReviewStatus() != 1) {
            return Result.fail("该用户无待审核的头像");
        }

        user.setIcon(user.getPendingIcon());
        user.setPendingIcon(null);
        user.setIconReviewStatus(0);
        userMapper.updateById(user);

        sendNotification(userId, "头像审核通过", "您的新头像已通过审核");
        log.info("头像审核通过: userId={}", userId);
        return Result.okMsg("头像审核通过");
    }

    @Override
    @Transactional
    public Result rejectIcon(Long userId, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) return Result.fail("用户不存在");
        if (user.getIconReviewStatus() != 1) {
            return Result.fail("该用户无待审核的头像");
        }

        user.setPendingIcon(null);
        user.setIconReviewStatus(2);
        userMapper.updateById(user);

        String msg = "您的新头像未通过审核";
        if (reason != null && !reason.isEmpty()) {
            msg += "，原因：" + reason;
        }
        sendNotification(userId, "头像审核未通过", msg);
        log.info("头像审核拒绝: userId={}, reason={}", userId, reason);
        return Result.okMsg("已拒绝头像修改");
    }

    @Override
    public Result getPendingCount() {
        long nicknameCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .in(User::getNicknameReviewStatus, 1, 3));
        long iconCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getIconReviewStatus, 1));
        Map<String, Object> data = new HashMap<>();
        data.put("nicknameCount", nicknameCount);
        data.put("iconCount", iconCount);
        return Result.ok(data);
    }

    private void sendNotification(Long userId, String title, String content) {
        try {
            SendNotificationFormDTO dto = new SendNotificationFormDTO();
            dto.setUserId(userId);
            dto.setTitle(title);
            dto.setContent(content);
            dto.setType("profile_review");
            notificationService.sendNotification(dto);
        } catch (Exception e) {
            log.error("发送审核通知失败: userId={}", userId, e);
        }
    }

}
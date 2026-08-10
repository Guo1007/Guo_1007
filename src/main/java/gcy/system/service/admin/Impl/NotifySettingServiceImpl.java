package gcy.system.service.admin.Impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.AdminNotifySetting;
import gcy.system.entity.pojo.User;
import gcy.system.mapper.AdminNotifySettingMapper;
import gcy.system.mapper.UserMapper;
import gcy.system.service.admin.INotifySettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员通知设置服务实现。
 *
 * @author 郭名城
 * @date 2026-08-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotifySettingServiceImpl implements INotifySettingService {

    /** 配置固定 ID */
    private static final Long SETTING_ID = 1L;

    private final AdminNotifySettingMapper adminNotifySettingMapper;

    private final UserMapper userMapper;

    @Override
    public Result getSetting() {
        AdminNotifySetting setting = adminNotifySettingMapper.selectById(SETTING_ID);
        Map<String, Object> data = new HashMap<>();
        data.put("enabled", setting != null && setting.getEnabled() != null && setting.getEnabled() == 1);
        data.put("adminIds", parseIds(setting != null ? setting.getAdminIds() : null));
        data.put("admins", listAdmins());
        return Result.ok(data);
    }

    @Override
    @Transactional
    public Result saveSetting(Boolean enabled, List<Long> adminIds) {
        AdminNotifySetting setting = adminNotifySettingMapper.selectById(SETTING_ID);
        if (setting == null) {
            setting = new AdminNotifySetting();
            setting.setId(SETTING_ID);
        }
        setting.setEnabled(Boolean.TRUE.equals(enabled) ? 1 : 0);
        setting.setAdminIds(adminIds == null || adminIds.isEmpty()
                ? null
                : adminIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        setting.setUpdateTime(LocalDateTime.now());
        if (adminNotifySettingMapper.selectById(SETTING_ID) == null) {
            adminNotifySettingMapper.insert(setting);
        } else {
            adminNotifySettingMapper.updateById(setting);
        }
        log.info("管理员通知配置已保存: enabled={}, adminIds={}", enabled, adminIds);
        return Result.ok();
    }

    @Override
    public List<Map<String, Object>> listAdmins() {
        List<User> admins = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getIsAdmin, 1)
                        .eq(User::getDeleted, 0));
        return admins.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("userName", u.getUserName());
            m.put("email", u.getEmail());
            return m;
        }).collect(Collectors.toList());
    }

    /**
     * 解析逗号分隔的管理员ID字符串。
     */
    public static List<Long> parseIds(String adminIds) {
        if (StrUtil.isBlank(adminIds)) {
            return List.of();
        }
        return Arrays.stream(adminIds.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}

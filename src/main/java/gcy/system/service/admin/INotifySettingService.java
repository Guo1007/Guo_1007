package gcy.system.service.admin;

import gcy.system.entity.dto.Result;

import java.util.List;
import java.util.Map;

/**
 * 管理员通知设置服务接口。
 *
 * @author 郭名城
 * @date 2026-08-08
 */
public interface INotifySettingService {

    /**
     * 获取当前通知配置及可选的管理员列表。
     *
     * @return 包含 enabled、adminIds、管理员列表的操作结果
     */
    Result getSetting();

    /**
     * 保存通知配置（开关 + 接收管理员ID列表）。
     *
     * @param enabled  是否开启
     * @param adminIds 接收通知的管理员ID列表
     * @return 保存结果
     */
    Result saveSetting(Boolean enabled, List<Long> adminIds);

    /**
     * 查询所有管理员（is_admin=1）的简易信息（id、用户名、邮箱）。
     *
     * @return 管理员信息列表
     */
    List<Map<String, Object>> listAdmins();
}

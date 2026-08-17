package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.AdminNotifySetting;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员邮件通知配置 Mapper。
 *
 * @author 郭名城
 * @date 2026-08-08
 */
@Mapper
public interface AdminNotifySettingMapper extends BaseMapper<AdminNotifySetting> {

    /**
     * 按 notify_type 原子写入配置（存在则更新，不存在则插入）。
     * <p>
     * 利用 notify_type 唯一索引 + ON DUPLICATE KEY UPDATE，避免并发保存时
     * "查无记录→重复插入"触发的唯一索引冲突。
     * </p>
     *
     * @param setting 配置内容（notify_type、enabled、admin_ids）
     */
    @Insert("INSERT INTO admin_notify_setting (notify_type, enabled, admin_ids, update_time) " +
            "VALUES (#{notifyType}, #{enabled}, #{adminIds}, NOW()) " +
            "ON DUPLICATE KEY UPDATE enabled = VALUES(enabled), " +
            "admin_ids = VALUES(admin_ids), update_time = NOW()")
    void upsertByNotifyType(AdminNotifySetting setting);
}

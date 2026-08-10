package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.AdminNotifySetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员邮件通知配置 Mapper。
 *
 * @author 郭名城
 * @date 2026-08-08
 */
@Mapper
public interface AdminNotifySettingMapper extends BaseMapper<AdminNotifySetting> {
}

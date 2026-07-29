package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.UserNotification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户通知 Mapper 接口。
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供对 user_notification 表的通用 CRUD 操作，
 * 包括增删改查、分页查询等基础数据库操作。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Mapper
public interface UserNotificationMapper extends BaseMapper<UserNotification> {
}

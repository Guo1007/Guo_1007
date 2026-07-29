package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知数据访问层接口。
 * <p>继承 MyBatis-Plus 的 BaseMapper，自动获得对 notification 表的通用 CRUD 操作能力，
 * 包括增删改查、分页查询、条件查询等基础数据库操作。</p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}

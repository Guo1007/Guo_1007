package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问层接口，继承 MyBatis-Plus 的 BaseMapper，
 * 提供对用户表（User）的基础 CRUD 操作（增删改查）。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}

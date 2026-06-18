package gcy.system.mapper.admin;

import gcy.system.entity.pojo.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserManageMapper extends BaseMapper<User> {
}

package gcy.system.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserManageMapper extends BaseMapper<User> {
}

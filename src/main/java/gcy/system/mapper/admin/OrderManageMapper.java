package gcy.system.mapper.admin;

import gcy.system.entity.pojo.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderManageMapper extends BaseMapper<Order> {
}

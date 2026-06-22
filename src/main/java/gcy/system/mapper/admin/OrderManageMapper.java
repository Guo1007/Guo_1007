package gcy.system.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderManageMapper extends BaseMapper<Order> {
}

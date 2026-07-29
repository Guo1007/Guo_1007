package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.FurnitureType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 家具类型Mapper接口。
 * 继承MyBatis-Plus的BaseMapper，提供对furniture_type表的CRUD操作能力。
 * 无需编写SQL即可使用内置的增删改查方法。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Mapper
public interface FurnitureTypeMapper extends BaseMapper<FurnitureType> {

}

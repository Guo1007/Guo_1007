package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.SpecValue;
import org.apache.ibatis.annotations.Mapper;

/**
 * 规格值数据访问层接口。
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供对 spec_value 表的通用 CRUD 操作，
 * 包括根据 ID 查询、批量查询、插入、更新、删除等基础数据库操作。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Mapper
public interface SpecValueMapper extends BaseMapper<SpecValue> {
}
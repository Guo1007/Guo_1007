package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.SpecGroup;
import org.apache.ibatis.annotations.Mapper;

/**
 * 规格分组数据访问层接口。
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供对 spec_group 表的基础 CRUD 操作，
 * 包括插入、删除、更新、查询以及分页查询等通用数据库操作。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Mapper
public interface SpecGroupMapper extends BaseMapper<SpecGroup> {
}
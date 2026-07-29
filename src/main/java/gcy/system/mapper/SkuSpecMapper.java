package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.SkuSpec;
import org.apache.ibatis.annotations.Mapper;

/**
 * SKU规格 Mapper 接口
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供对 sku_spec 表的通用 CRUD 操作，
 * 包括根据 ID 查询、批量查询、插入、更新、删除等数据库操作。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Mapper
public interface SkuSpecMapper extends BaseMapper<SkuSpec> {
}
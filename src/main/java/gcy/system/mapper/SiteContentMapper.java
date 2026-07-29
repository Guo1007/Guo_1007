package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.SiteContent;
import org.apache.ibatis.annotations.Mapper;

/**
 * SiteContent 数据库映射接口。
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供对 site_content 表的基础 CRUD 操作，
 * 包括插入、更新、删除、按 ID 查询、条件查询、分页查询等通用数据库操作。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Mapper
public interface SiteContentMapper extends BaseMapper<SiteContent> {
}

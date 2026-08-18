package gcy.system.service;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.SiteContent;

/**
 * 站点内容服务接口，提供站点内容（如首页板块、公告等）的查询与管理功能。
 * 将数据库操作从控制器层下沉到服务层，遵循分层架构规范。
 *
 * @author 郭名城
 * @date 2026-08-12
 */
public interface ISiteContentService {

    /**
     * 获取所有启用的站点内容，按 sectionGroup 分组归类
     *
     * @return 包含分组后站点内容数据的统一返回结果
     */
    Result getActiveSiteContentGrouped();

    /**
     * 查询所有网站内容列表，按板块分组和排序序号升序排列
     *
     * @return 包含网站内容列表的统一返回结果
     */
    Result listAll();

    /**
     * 保存网站内容（新增或更新），根据 sectionKey 判断处理
     *
     * @param form 网站内容表单数据
     * @return 操作结果
     */
    Result saveOrUpdateContent(SiteContent form);

    /**
     * 切换网站内容的启用/禁用状态
     *
     * @param id 网站内容记录的主键 ID
     * @return 切换后的启用状态值（0 或 1）
     */
    Result toggleStatus(Long id);

    /**
     * 删除指定 ID 的网站内容记录
     *
     * @param id 网站内容记录的主键 ID
     * @return 操作结果
     */
    Result deleteById(Long id);
}
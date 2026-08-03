package gcy.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.SiteContent;
import gcy.system.mapper.SiteContentMapper;
import gcy.system.integration.OssService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 网站内容管理控制器
 * <p>
 * 提供网站内容（如首页板块、公告等）的增删改查及图片上传接口。
 * 基础路径：/admin/site-content
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@RestController
@RequestMapping("/admin/site-content")
@RequiredArgsConstructor
public class SiteContentManageController {

    private final SiteContentMapper siteContentMapper;

    private final OssService ossService;

    /**
     * 查询所有网站内容列表
     * <p>
     * 按板块分组（sectionGroup）和排序序号（sortOrder）升序排列返回。
     * </p>
     *
     * @return 包含网站内容列表的统一响应结果
     */
    @GetMapping
    public Result list() {
        return Result.ok(siteContentMapper.selectList(
                new LambdaQueryWrapper<SiteContent>()
                        .orderByAsc(SiteContent::getSectionGroup)
                        .orderByAsc(SiteContent::getSortOrder)));
    }

    /**
     * 保存网站内容（新增或更新）
     * <p>
     * 根据 sectionKey 判断是否已存在记录：存在则更新，不存在则新增。
     * sectionKey 为必填项。
     * </p>
     *
     * @param form 网站内容表单数据，通过请求体传入
     * @return 操作结果的统一响应，成功或失败
     */
    @PostMapping
    public Result save(@RequestBody SiteContent form) {
        if (form.getSectionKey() == null || form.getSectionKey().isBlank()) {
            return Result.fail("sectionKey 不能为空");
        }
        SiteContent exist = siteContentMapper.selectOne(
                new LambdaQueryWrapper<SiteContent>().eq(SiteContent::getSectionKey, form.getSectionKey()));
        form.setUpdatedAt(LocalDateTime.now());
        if (exist != null) {
            form.setId(exist.getId());
            siteContentMapper.updateById(form);
        } else {
            siteContentMapper.insert(form);
        }
        return Result.ok();
    }

    /**
     * 切换网站内容的启用/禁用状态
     * <p>
     * 将指定 ID 记录的 isActive 字段在 0 和 1 之间切换。
     * </p>
     *
     * @param id 网站内容记录的主键 ID
     * @return 切换后的启用状态值（0 或 1）
     */
    @PutMapping("/{id}/toggle")
    public Result toggle(@PathVariable Long id) {
        SiteContent sc = siteContentMapper.selectById(id);
        if (sc == null) return Result.fail("记录不存在");
        sc.setIsActive(sc.getIsActive() == 1 ? 0 : 1);
        sc.setUpdatedAt(LocalDateTime.now());
        siteContentMapper.updateById(sc);
        return Result.ok(sc.getIsActive());
    }

    /**
     * 删除指定 ID 的网站内容记录
     *
     * @param id 网站内容记录的主键 ID
     * @return 操作结果的统一响应
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        siteContentMapper.deleteById(id);
        return Result.ok();
    }

    /**
     * 上传网站内容相关的图片文件
     * <p>
     * 将上传的图片存储到 OSS 的 site 目录下，并返回可访问的 URL 地址。
     * </p>
     *
     * @param file 上传的图片文件，表单参数名为 "file"
     * @return 上传成功后的图片访问 URL
     */
    @PostMapping("/upload")
    public Result uploadImage(@RequestParam("file") MultipartFile file) {
        String url = ossService.upload(file, "site");
        return Result.ok(url);
    }
}

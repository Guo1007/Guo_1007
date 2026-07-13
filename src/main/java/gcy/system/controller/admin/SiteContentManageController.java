package gcy.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.SiteContent;
import gcy.system.mapper.SiteContentMapper;
import gcy.system.service.OssService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/site-content")
@RequiredArgsConstructor
public class SiteContentManageController {

    private final SiteContentMapper siteContentMapper;

    private final OssService ossService;

    /** 列表 */
    @GetMapping
    public Result list() {
        return Result.ok(siteContentMapper.selectList(
                new LambdaQueryWrapper<SiteContent>()
                        .orderByAsc(SiteContent::getSectionGroup)
                        .orderByAsc(SiteContent::getSortOrder)));
    }

    /** 保存（新增或更新，按 section_key 唯一） */
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

    /** 切换启用状态 */
    @PutMapping("/{id}/toggle")
    public Result toggle(@PathVariable Long id) {
        SiteContent sc = siteContentMapper.selectById(id);
        if (sc == null) return Result.fail("记录不存在");
        sc.setIsActive(sc.getIsActive() == 1 ? 0 : 1);
        sc.setUpdatedAt(LocalDateTime.now());
        siteContentMapper.updateById(sc);
        return Result.ok(sc.getIsActive());
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        siteContentMapper.deleteById(id);
        return Result.ok();
    }

    /** 上传图片 */
    @PostMapping("/upload")
    public Result uploadImage(@RequestParam("file") MultipartFile file) {
        String url = ossService.upload(file, "site");
        return Result.ok(url);
    }
}

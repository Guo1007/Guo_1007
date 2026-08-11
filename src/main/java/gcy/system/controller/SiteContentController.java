package gcy.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.SiteContent;
import gcy.system.mapper.SiteContentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 站点内容控制器
 * <p>
 * 提供公开的站点内容查询接口，用于获取所有已启用的站点内容，
 * 并按分组归类返回，供前端展示使用。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Tag(name = "站点内容", description = "站点内容相关接口")
@RestController
@RequiredArgsConstructor
public class SiteContentController {

    private final SiteContentMapper siteContentMapper;

    /**
     * 获取所有启用的站点内容，按分组归类
     * <p>
     * 查询数据库中所有标记为启用（isActive = 1）的站点内容记录，
     * 按排序字段升序排列后，以 sectionGroup 为键进行分组，
     * 返回一个分组后的有序映射结构。
     * </p>
     *
     * @return 包含分组后的站点内容数据的结果对象，key 为分组名，value 为该分组下的内容列表
     */
    @Operation(summary = "获取所有启用的站点内容")
    @GetMapping("/api/site-content")
    public Result getSiteContent() {
        List<SiteContent> list = siteContentMapper.selectList(
                new LambdaQueryWrapper<SiteContent>()
                        .eq(SiteContent::getIsActive, 1)
                        .orderByAsc(SiteContent::getSortOrder)
        );

        // 按 sectionGroup 分组，保持插入顺序；sectionGroup 为 null 的记录归入空分组，避免 groupingBy 抛 NPE
        Map<String, List<SiteContent>> grouped = list.stream()
                .filter(s -> s.getSectionGroup() != null)
                .collect(Collectors.groupingBy(
                        SiteContent::getSectionGroup,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return Result.ok(grouped);
    }
}

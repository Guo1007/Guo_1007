package gcy.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.SiteContent;
import gcy.system.mapper.SiteContentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class SiteContentController {

    private final SiteContentMapper siteContentMapper;

    /**
     * 公开接口：返回所有启用的站点内容，按分组归类
     */
    @GetMapping("/api/site-content")
    public Result getSiteContent() {
        List<SiteContent> list = siteContentMapper.selectList(
                new LambdaQueryWrapper<SiteContent>()
                        .eq(SiteContent::getIsActive, 1)
                        .orderByAsc(SiteContent::getSortOrder)
        );

        // 按 sectionGroup 分组，保持插入顺序
        Map<String, List<SiteContent>> grouped = list.stream()
                .collect(Collectors.groupingBy(
                        SiteContent::getSectionGroup,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return Result.ok(grouped);
    }
}

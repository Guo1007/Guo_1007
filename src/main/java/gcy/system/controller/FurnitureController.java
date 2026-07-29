package gcy.system.controller;

import gcy.system.entity.dto.Result;
import gcy.system.service.IFurnitureService;
import gcy.system.service.ISpecService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 家具管理控制器
 * <p>提供家具列表查询、热销排行、品牌筛选、家具详情及规格查询等接口。
 * 所有接口均以 {@code /furniture} 为根路径，返回统一的 {@link Result} 对象。</p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@RestController
@RequestMapping("/furniture")
@RequiredArgsConstructor
public class FurnitureController {

    private final IFurnitureService furnitureService;

    private final ISpecService specService;

    /**
     * 分页查询家具列表
     * <p>支持按类型、关键词、库存状态、品牌进行筛选，支持排序和推荐过滤。</p>
     *
     * @param typeId       家具类型ID，可选，用于按类型筛选
     * @param current      当前页码，默认值为1
     * @param size         每页条数，默认值为10
     * @param keyword      搜索关键词，可选，用于模糊搜索家具名称
     * @param stockStatus  库存状态，可选，用于按库存状态筛选
     * @param brand        品牌名称，可选，用于按品牌筛选
     * @param sortBy       排序字段，可选，指定按哪个字段排序
     * @param sortOrder    排序方式，可选，asc 升序 / desc 降序
     * @param isRecommended 是否推荐，可选，1表示只查询推荐家具
     * @return 包含分页家具列表数据的统一响应结果
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(required = false) Long typeId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) Integer isRecommended) {
        return furnitureService.getFurnitureByType(typeId, current, size, keyword, stockStatus, brand, sortBy, sortOrder, isRecommended);
    }

    /**
     * 查询热销家具排行
     * <p>返回销量最高的前N款家具。</p>
     *
     * @param limit 返回的家具数量上限，默认值为8
     * @return 包含热销家具列表数据的统一响应结果
     */
    @GetMapping("/top-selling")
    public Result topSelling(@RequestParam(defaultValue = "8") Integer limit) {
        return furnitureService.getTopSelling(limit);
    }

    /**
     * 查询家具品牌列表
     * <p>可按家具类型筛选，返回该类型下所有可用的品牌。</p>
     *
     * @param typeId 家具类型ID，可选，不传则返回所有类型的品牌
     * @return 包含品牌列表数据的统一响应结果
     */
    @GetMapping("/brands")
    public Result getFurnitureBrands(@RequestParam(required = false) Long typeId) {
        return furnitureService.getFurnitureBrandsByTypeId(typeId);
    }

    /**
     * 根据ID查询家具详情
     * <p>通过路径变量传入家具ID，返回该家具的完整信息。</p>
     *
     * @param id 家具ID，必填，通过URL路径传入
     * @return 包含家具详情数据的统一响应结果
     */
    @GetMapping("/{id}")
    public Result queryFurnitureById(@PathVariable Long id) {
        return furnitureService.queryFurnitureById(id);
    }

    /**
     * 查询家具的可用规格和SKU
     * <p>根据家具ID返回该家具所有可用的规格组合及对应的SKU信息。</p>
     *
     * @param id 家具ID，必填，通过URL路径传入
     * @return 包含规格和SKU列表数据的统一响应结果
     */
    @GetMapping("/{id}/specs")
    public Result getFurnitureSpecs(@PathVariable Long id) {
        return specService.getAvailableSpecAndSku(id);
    }

}

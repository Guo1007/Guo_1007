package gcy.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.Furniture;

/**
 * 家具服务接口，定义家具相关的业务操作。
 * <p>
 * 提供家具信息的查询、分类检索、热门推荐以及品牌获取等功能。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface IFurnitureService extends IService<Furniture> {

    /**
     * 根据家具ID查询单个家具的详细信息。
     *
     * @param id 家具的唯一标识ID
     * @return 包含家具详情的结果对象
     */
    Result queryFurnitureById(Long id);

    /**
     * 根据家具类型及其他筛选条件分页查询家具列表。
     * <p>
     * 支持按名称、库存状态、品牌进行筛选，以及按指定字段排序，
     * 同时可筛选推荐家具。
     * </p>
     *
     * @param typeId        家具类型ID
     * @param current       当前页码
     * @param size          每页记录数
     * @param fName         家具名称（模糊匹配）
     * @param stockStatus   库存状态筛选条件
     * @param brand         品牌筛选条件
     * @param sortBy        排序字段
     * @param sortOrder     排序方式（升序/降序）
     * @param isRecommended 是否推荐（1表示推荐，0或null表示不限制）
     * @return 包含分页家具列表的结果对象
     */
    Result getFurnitureByType(Long typeId, Integer current, Integer size,
                              String fName, String stockStatus, String brand,
                              String sortBy, String sortOrder,
                              Integer isRecommended);

    /**
     * 获取销量最高的家具列表。
     * <p>
     * 根据销售数量降序排列，返回指定数量的热销家具。
     * </p>
     *
     * @param limit 返回的家具数量上限
     * @return 包含热销家具列表的结果对象
     */
    Result getTopSelling(Integer limit);

    /**
     * 根据家具类型ID获取该类型下所有可用的品牌列表。
     *
     * @param id 家具类型ID
     * @return 包含品牌列表的结果对象
     */
    Result getFurnitureBrandsByTypeId(Long id);

}

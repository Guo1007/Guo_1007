package gcy.system.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.AdminFurnitureFormDTO;
import gcy.system.entity.pojo.Furniture;

/**
 * 家具管理服务接口，定义家具信息的增删改查等核心业务操作。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface IFurnitureManageService extends IService<Furniture> {

    /**
     * 分页查询家具列表，支持按家具类型、名称、库存状态和品牌进行筛选。
     *
     * @param current     当前页码
     * @param size        每页记录数
     * @param typeId      家具类型ID，可为null表示不按类型筛选
     * @param fName       家具名称关键字，可为null表示不按名称筛选
     * @param stockStatus 库存状态，可为null表示不按库存状态筛选
     * @param brand       品牌名称，可为null表示不按品牌筛选
     * @return 包含分页家具数据的Result对象
     */
    Result getFurnitureList(Integer current, Integer size, Long typeId, String fName,
                            String stockStatus, String brand);

    /**
     * 新增一件家具信息。
     *
     * @param dto 包含新家具完整信息的表单数据传输对象
     * @return 包含操作结果的Result对象
     */
    Result addFurniture(AdminFurnitureFormDTO dto);

    /**
     * 编辑已有家具的信息。
     *
     * @param dto 包含更新后家具信息的表单数据传输对象
     * @return 包含操作结果的Result对象
     */
    Result editFurniture(AdminFurnitureFormDTO dto);

    /**
     * 根据家具ID删除指定家具。
     *
     * @param furnitureId 要删除的家具唯一标识ID
     * @return 包含操作结果的Result对象
     */
    Result deleteFurniture(Long furnitureId);

}

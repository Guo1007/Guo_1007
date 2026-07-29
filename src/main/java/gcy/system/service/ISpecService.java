package gcy.system.service;

import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.FurnitureSpecDTO;

/**
 * 家具规格与SKU服务接口。
 * <p>
 * 提供家具规格（Spec）和库存单位（SKU）的查询与保存等核心业务能力，
 * 包括根据家具ID获取规格及SKU信息、保存规格及SKU数据、
 * 以及获取可用规格与SKU列表。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface ISpecService {

    /**
     * 根据家具ID获取该家具关联的所有规格及SKU信息。
     * <p>
     * 该方法通过指定的家具ID，查询并返回该家具下已配置的
     * 全部规格选项和对应的SKU数据，通常用于规格配置详情展示。
     * </p>
     *
     * @param furnitureId 家具的唯一标识ID，用于定位目标家具
     * @return 包含对应家具规格及SKU信息的 {@link Result} 对象
     */
    Result getSpecAndSkuByFurnitureId(Long furnitureId);

    /**
     * 保存或更新家具的规格及SKU数据。
     * <p>
     * 接收前端提交的完整规格与SKU传输对象，执行业务校验后
     * 将其持久化到数据库。若对应记录已存在则更新，否则新增。
     * </p>
     *
     * @param dto 包含规格及SKU信息的传输对象 {@link FurnitureSpecDTO}，
     *            封装了家具ID、规格列表、SKU列表等数据
     * @return 表示保存操作结果的 {@link Result} 对象，包含成功或失败状态
     */
    Result saveSpecAndSku(FurnitureSpecDTO dto);

    /**
     * 获取指定家具当前可用的规格及SKU信息。
     * <p>
     * 与 {@link #getSpecAndSkuByFurnitureId(Long)} 不同，该方法
     * 仅返回状态为可用（例如已上架、有库存等）的规格与SKU，
     * 常用于用户端选择商品规格时的展示场景。
     * </p>
     *
     * @param furnitureId 家具的唯一标识ID，用于定位目标家具
     * @return 包含当前可用规格及SKU信息的 {@link Result} 对象
     */
    Result getAvailableSpecAndSku(Long furnitureId);
}
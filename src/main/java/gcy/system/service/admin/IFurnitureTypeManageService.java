package gcy.system.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.AdminFurnitureTypeFormDTO;
import gcy.system.entity.pojo.FurnitureType;

/**
 * 家具类型管理服务接口。
 * 提供家具类型的增删改查等核心管理功能，包括添加、编辑、删除、按ID查询以及分页列表查询。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface IFurnitureTypeManageService extends IService<FurnitureType> {

    /**
     * 添加新的家具类型。
     * 根据传入的表单数据创建一条新的家具类型记录。
     *
     * @param dto 包含待添加家具类型信息的表单数据传输对象
     * @return 操作结果，包含成功与否的状态及相关提示信息
     */
    Result addFurnitureType(AdminFurnitureTypeFormDTO dto);

    /**
     * 编辑已有的家具类型。
     * 根据传入的表单数据更新对应家具类型的信息。
     *
     * @param dto 包含待更新家具类型信息的表单数据传输对象，其中必须携带要编辑的记录ID
     * @return 操作结果，包含成功与否的状态及相关提示信息
     */
    Result editFurnitureType(AdminFurnitureTypeFormDTO dto);

    /**
     * 删除指定的家具类型。
     * 根据主键ID删除对应的家具类型记录。
     *
     * @param id 要删除的家具类型的唯一标识ID
     * @return 操作结果，包含成功与否的状态及相关提示信息
     */
    Result deleteFurnitureType(Long id);

    /**
     * 根据ID获取单个家具类型的详细信息。
     *
     * @param id 要查询的家具类型的唯一标识ID
     * @return 操作结果，包含对应家具类型的详细信息；若不存在则返回相应错误状态
     */
    Result getFurnitureTypeById(Long id);

    /**
     * 分页查询家具类型列表。
     * 支持按类型名称进行模糊搜索，返回分页后的家具类型数据。
     *
     * @param current  当前页码，从1开始
     * @param size     每页显示的记录条数
     * @param typeName 家具类型名称，用于模糊搜索匹配；可为空，为空时返回全部
     * @return 操作结果，包含分页后的家具类型列表及分页信息
     */
    Result getFurnitureTypeList(Integer current, Integer size, String typeName);
}

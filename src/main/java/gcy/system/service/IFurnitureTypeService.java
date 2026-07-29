package gcy.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.FurnitureType;

/**
 * 家具类型服务接口，定义家具类型相关的业务操作。
 * 继承MyBatis-Plus的IService，提供基础的CRUD能力。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface IFurnitureTypeService extends IService<FurnitureType> {

    /**
     * 查询家具类型列表，返回所有可用的家具类型信息。
     *
     * @return 包含家具类型列表的结果对象
     */
    Result queryFurnitureTypeList();

}

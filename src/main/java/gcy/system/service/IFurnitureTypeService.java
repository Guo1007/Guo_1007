package gcy.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.FurnitureType;

public interface IFurnitureTypeService extends IService<FurnitureType> {

    Result queryFurnitureTypeList();

}

package gcy.system.service;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.FurnitureType;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IFurnitureTypeService extends IService<FurnitureType> {

    Result queryFurnitureTypeList();

}

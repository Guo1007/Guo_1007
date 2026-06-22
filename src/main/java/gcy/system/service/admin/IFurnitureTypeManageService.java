package gcy.system.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.AdminFurnitureTypeFormDTO;
import gcy.system.entity.pojo.FurnitureType;

public interface IFurnitureTypeManageService extends IService<FurnitureType> {

    Result addFurnitureType(AdminFurnitureTypeFormDTO dto);

    Result editFurnitureType(AdminFurnitureTypeFormDTO dto);

    Result deleteFurnitureType(Long id);

    Result getFurnitureTypeById(Long id);

    Result getFurnitureTypeList(Integer current, Integer size, String typeName);
}
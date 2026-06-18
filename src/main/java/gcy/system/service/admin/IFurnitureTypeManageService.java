package gcy.system.service.admin;

import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.AdminFurnitureTypeFormDTO;
import gcy.system.entity.pojo.FurnitureType;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IFurnitureTypeManageService extends IService<FurnitureType> {

    Result addFurnitureType(AdminFurnitureTypeFormDTO dto);

    Result editFurnitureType(AdminFurnitureTypeFormDTO dto);

    Result deleteFurnitureType(Long id);

    Result getFurnitureTypeById(Long id);

    Result getFurnitureTypeList(Integer current, Integer size, String typeName);
}
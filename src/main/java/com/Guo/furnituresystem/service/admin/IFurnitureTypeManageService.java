package com.Guo.furnituresystem.service.admin;

import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.entity.dto.admin.AdminFurnitureTypeFormDTO;
import com.Guo.furnituresystem.entity.pojo.FurnitureType;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IFurnitureTypeManageService extends IService<FurnitureType> {

    Result addFurnitureType(AdminFurnitureTypeFormDTO dto);

    Result editFurnitureType(AdminFurnitureTypeFormDTO dto);

    Result deleteFurnitureType(Long id);

    Result getFurnitureTypeById(Long id);

    Result getFurnitureTypeList(Integer current, Integer size, String typeName);
}
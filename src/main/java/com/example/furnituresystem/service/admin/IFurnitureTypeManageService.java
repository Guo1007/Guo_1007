package com.example.furnituresystem.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.dto.admin.AdminFurnitureTypeFormDTO;
import com.example.furnituresystem.entity.pojo.FurnitureType;

public interface IFurnitureTypeManageService extends IService<FurnitureType> {

    Result addFurnitureType(AdminFurnitureTypeFormDTO dto);

    Result editFurnitureType(AdminFurnitureTypeFormDTO dto);

    Result deleteFurnitureType(Long id);

    Result getFurnitureTypeById(Long id);

    Result getFurnitureTypeList(Integer current, Integer size, String typeName);
}
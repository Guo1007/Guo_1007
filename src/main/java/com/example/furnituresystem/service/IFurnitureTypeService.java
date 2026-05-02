package com.example.furnituresystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.pojo.FurnitureType;

public interface IFurnitureTypeService extends IService<FurnitureType> {

    Result queryFurnitureTypeList();

}

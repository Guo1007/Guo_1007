package com.Guo.furnituresystem.service;

import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.entity.pojo.FurnitureType;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IFurnitureTypeService extends IService<FurnitureType> {

    Result queryFurnitureTypeList();

}

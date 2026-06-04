package com.Guo.furnituresystem.service;

import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.entity.pojo.Furniture;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IFurnitureService extends IService<Furniture> {

    Result queryFurnitureById(Long id);

    Result getFurnitureByType(Long typeId, Integer current, Integer size,
                              String fName, String stockStatus, String brand);

    Result getFurnitureBrandsByTypeId(Long id);

}

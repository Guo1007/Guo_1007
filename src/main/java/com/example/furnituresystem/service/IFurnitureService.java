package com.example.furnituresystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.pojo.Furniture;

public interface IFurnitureService extends IService<Furniture> {

    Result queryFurnitureById(Long id);

    Result getFurnitureByType(Long typeId, Integer current, Integer size,
                              String fName, String stockStatus, String brand);

    Result getFurnitureBrandsByTypeId(Long id);

}

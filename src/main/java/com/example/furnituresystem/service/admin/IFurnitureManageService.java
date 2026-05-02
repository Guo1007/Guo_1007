package com.example.furnituresystem.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.dto.admin.AdminFurnitureFormDTO;
import com.example.furnituresystem.entity.pojo.Furniture;

public interface IFurnitureManageService extends IService<Furniture> {

    Result getFurnitureList(Integer current, Integer size, Long typeId, String fName,
                            String stockStatus, String brand);

    Result addFurniture(AdminFurnitureFormDTO dto);

    Result editFurniture(AdminFurnitureFormDTO dto);

    Result deleteFurniture(Long furnitureId);

}

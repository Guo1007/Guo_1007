package com.Guo.furnituresystem.service.admin;

import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.entity.dto.admin.AdminFurnitureFormDTO;
import com.Guo.furnituresystem.entity.pojo.Furniture;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IFurnitureManageService extends IService<Furniture> {

    Result getFurnitureList(Integer current, Integer size, Long typeId, String fName,
                            String stockStatus, String brand);

    Result addFurniture(AdminFurnitureFormDTO dto);

    Result editFurniture(AdminFurnitureFormDTO dto);

    Result deleteFurniture(Long furnitureId);

}

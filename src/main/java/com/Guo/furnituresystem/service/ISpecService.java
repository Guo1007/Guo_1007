package com.Guo.furnituresystem.service;

import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.entity.dto.admin.FurnitureSpecDTO;

public interface ISpecService {

    Result getSpecAndSkuByFurnitureId(Long furnitureId);

    Result saveSpecAndSku(FurnitureSpecDTO dto);

    Result getAvailableSpecAndSku(Long furnitureId);
}
package gcy.system.service;

import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.FurnitureSpecDTO;

public interface ISpecService {

    Result getSpecAndSkuByFurnitureId(Long furnitureId);

    Result saveSpecAndSku(FurnitureSpecDTO dto);

    Result getAvailableSpecAndSku(Long furnitureId);
}
package gcy.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.Furniture;

public interface IFurnitureService extends IService<Furniture> {

    Result queryFurnitureById(Long id);

    Result getFurnitureByType(Long typeId, Integer current, Integer size,
                              String fName, String stockStatus, String brand);

    Result getFurnitureBrandsByTypeId(Long id);

}

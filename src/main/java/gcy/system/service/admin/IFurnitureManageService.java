package gcy.system.service.admin;

import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.AdminFurnitureFormDTO;
import gcy.system.entity.pojo.Furniture;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IFurnitureManageService extends IService<Furniture> {

    Result getFurnitureList(Integer current, Integer size, Long typeId, String fName,
                            String stockStatus, String brand);

    Result addFurniture(AdminFurnitureFormDTO dto);

    Result editFurniture(AdminFurnitureFormDTO dto);

    Result deleteFurniture(Long furnitureId);

}

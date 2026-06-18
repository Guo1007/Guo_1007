package gcy.system.controller;

import gcy.system.entity.dto.Result;
import gcy.system.service.IFurnitureTypeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/furniture_type")
public class FurnitureTypeController {

    @Resource
    private IFurnitureTypeService furnitureTypeService;

    @GetMapping("/list")
    public Result getTypeList() {
        return furnitureTypeService.queryFurnitureTypeList();
    }

}

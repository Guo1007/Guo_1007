package gcy.system.controller;

import gcy.system.entity.dto.Result;
import gcy.system.service.IFurnitureTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/furniture_type")
@RequiredArgsConstructor
public class FurnitureTypeController {

    private final IFurnitureTypeService furnitureTypeService;

    @GetMapping("/list")
    public Result getTypeList() {
        return furnitureTypeService.queryFurnitureTypeList();
    }

}

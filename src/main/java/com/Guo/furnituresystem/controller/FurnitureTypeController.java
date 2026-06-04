package com.Guo.furnituresystem.controller;

import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.service.IFurnitureTypeService;
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

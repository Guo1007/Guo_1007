package com.Guo.furnituresystem.controller;

import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.service.IFurnitureService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/furniture")
public class FurnitureController {

    @Resource
    private IFurnitureService furnitureService;

    @GetMapping("/list")
    public Result list(
            @RequestParam(required = false) Long typeId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fName,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) String brand) {
        String nameFilter = keyword != null ? keyword : fName;
        return furnitureService.getFurnitureByType(typeId, current, size, nameFilter, stockStatus, brand);
    }

    @GetMapping("/brands")
    public Result getFurnitureBrands(@RequestParam(required = false) Long typeId) {
        return furnitureService.getFurnitureBrandsByTypeId(typeId);
    }

    @GetMapping("/{id}")
    public Result queryFurnitureById(@PathVariable Long id) {
        return furnitureService.queryFurnitureById(id);
    }

}

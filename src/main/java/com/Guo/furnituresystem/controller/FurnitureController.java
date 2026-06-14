package com.Guo.furnituresystem.controller;

import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.service.IFurnitureService;
import com.Guo.furnituresystem.service.ISpecService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/furniture")
public class FurnitureController {

    @Resource
    private IFurnitureService furnitureService;

    @Resource
    private ISpecService specService;

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

    /**
     * 查询商品的规格+SKU（客户端展示用，只返回有库存可用的）
     */
    @GetMapping("/{id}/specs")
    public Result getFurnitureSpecs(@PathVariable Long id) {
        return specService.getAvailableSpecAndSku(id);
    }

}

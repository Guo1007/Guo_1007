package gcy.system.controller;

import gcy.system.entity.dto.Result;
import gcy.system.service.IFurnitureService;
import gcy.system.service.ISpecService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/furniture")
@RequiredArgsConstructor
public class FurnitureController {

    private final IFurnitureService furnitureService;

    private final ISpecService specService;

    @GetMapping("/list")
    public Result list(
            @RequestParam(required = false) Long typeId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) String brand) {
        return furnitureService.getFurnitureByType(typeId, current, size, keyword, stockStatus, brand);
    }

    @GetMapping("/brands")
    public Result getFurnitureBrands(@RequestParam(required = false) Long typeId) {
        return furnitureService.getFurnitureBrandsByTypeId(typeId);
    }

    @GetMapping("/{id}")
    public Result queryFurnitureById(@PathVariable Long id) {
        return furnitureService.queryFurnitureById(id);
    }

    @GetMapping("/{id}/specs")
    public Result getFurnitureSpecs(@PathVariable Long id) {
        return specService.getAvailableSpecAndSku(id);
    }

}

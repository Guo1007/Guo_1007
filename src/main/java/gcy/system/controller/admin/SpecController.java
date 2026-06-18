package gcy.system.controller.admin;

import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.FurnitureSpecDTO;
import gcy.system.service.ISpecService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/admin/spec")
public class SpecController {

    @Resource
    private ISpecService specService;

    /**
     * 查询商品的规格+SKU（管理端编辑用，返回全部包括禁用的）
     */
    @GetMapping("/{furnitureId}")
    public Result getSpecAndSku(@PathVariable Long furnitureId) {
        return specService.getSpecAndSkuByFurnitureId(furnitureId);
    }

    /**
     * 保存商品的规格与SKU（全量替换）
     */
    @PostMapping("/save")
    public Result saveSpecAndSku(@RequestBody FurnitureSpecDTO dto) {
        return specService.saveSpecAndSku(dto);
    }
}
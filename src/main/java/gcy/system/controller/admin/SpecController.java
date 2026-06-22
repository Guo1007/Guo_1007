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

    @GetMapping("/{furnitureId}")
    public Result getSpecAndSku(@PathVariable Long furnitureId) {
        return specService.getSpecAndSkuByFurnitureId(furnitureId);
    }

    @PostMapping("/save")
    public Result saveSpecAndSku(@RequestBody FurnitureSpecDTO dto) {
        return specService.saveSpecAndSku(dto);
    }
}
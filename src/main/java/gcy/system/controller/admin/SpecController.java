package gcy.system.controller.admin;

import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.FurnitureSpecDTO;
import gcy.system.service.ISpecService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/spec")
@RequiredArgsConstructor
public class SpecController {

    private final ISpecService specService;

    @GetMapping("/{furnitureId}")
    public Result getSpecAndSku(@PathVariable Long furnitureId) {
        return specService.getSpecAndSkuByFurnitureId(furnitureId);
    }

    @PostMapping("/save")
    public Result saveSpecAndSku(@RequestBody FurnitureSpecDTO dto) {
        return specService.saveSpecAndSku(dto);
    }
}
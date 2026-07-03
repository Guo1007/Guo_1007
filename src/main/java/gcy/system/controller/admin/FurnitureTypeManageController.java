package gcy.system.controller.admin;

import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.AdminFurnitureTypeFormDTO;
import gcy.system.exception.BusinessException;
import gcy.system.service.OssService;
import gcy.system.service.admin.IFurnitureTypeManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@RequestMapping("/admin/furniture_type")
@RequiredArgsConstructor
public class FurnitureTypeManageController {

    private final IFurnitureTypeManageService furnitureTypeManageService;

    private final OssService ossService;

    @PostMapping("/add")
    public Result addFurnitureType(@Valid @RequestBody AdminFurnitureTypeFormDTO dto) {
        return furnitureTypeManageService.addFurnitureType(dto);
    }

    @PutMapping("/update")
    public Result editFurnitureType(@Valid @RequestBody AdminFurnitureTypeFormDTO dto) {
        return furnitureTypeManageService.editFurnitureType(dto);
    }

    @PostMapping("/upload")
    public Result uploadTypeIcon(@RequestParam("file") MultipartFile file) {
        try {
            String url = ossService.upload(file, "type");
            return Result.ok(url);
        } catch (Exception e) {
            throw new BusinessException("上传失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteFurnitureType(@PathVariable Long id) {
        return furnitureTypeManageService.deleteFurnitureType(id);
    }

    @GetMapping("/info/{id}")
    public Result getFurnitureTypeInfo(@PathVariable Long id) {
        return furnitureTypeManageService.getFurnitureTypeById(id);
    }

    @GetMapping("/list")
    public Result getFurnitureTypeList(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String name) {
        return furnitureTypeManageService.getFurnitureTypeList(current, size, name);
    }
}
package com.Guo.furnituresystem.controller.admin;

import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.entity.dto.admin.AdminFurnitureTypeFormDTO;
import com.Guo.furnituresystem.exception.BusinessException;
import com.Guo.furnituresystem.service.admin.IFurnitureTypeManageService;
import com.Guo.furnituresystem.utils.FileUploadUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@Validated
@RequestMapping("/admin/furniture_type")
public class FurnitureTypeManageController {

    @Resource
    private IFurnitureTypeManageService furnitureTypeManageService;

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
            String url = FileUploadUtil.upload(file, "type");
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
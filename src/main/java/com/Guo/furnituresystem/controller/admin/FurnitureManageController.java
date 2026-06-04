package com.Guo.furnituresystem.controller.admin;

import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.entity.dto.admin.AdminFurnitureFormDTO;
import com.Guo.furnituresystem.exception.BusinessException;
import com.Guo.furnituresystem.service.admin.IFurnitureManageService;
import com.Guo.furnituresystem.utils.FileUploadUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@RequestMapping("/admin/furniture")
public class FurnitureManageController {

    @Resource
    private IFurnitureManageService furnitureManageService;

    @GetMapping("/list")
    public Result getFurnitureList(@RequestParam(defaultValue = "1") Integer current,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam(required = false) Long typeId,
                                   @RequestParam(required = false) String fName,
                                   @RequestParam(required = false) String stockStatus,
                                   @RequestParam(required = false) String brand) {
        return furnitureManageService.getFurnitureList(current, size, typeId, fName, stockStatus, brand);
    }

    @PostMapping("/add")
    public Result addFurniture(@RequestBody @Valid AdminFurnitureFormDTO dto) {
        return furnitureManageService.addFurniture(dto);
    }

    @PutMapping("/edit")
    public Result editFurniture(@RequestBody @Valid AdminFurnitureFormDTO dto) {
        return furnitureManageService.editFurniture(dto);
    }

    @PostMapping("/upload")
    public Result uploadFurnitureImage(@RequestParam("file") MultipartFile file) {
        try {
            String url = FileUploadUtil.upload(file, "furniture");
            return Result.ok(url);
        } catch (Exception e) {
            throw new BusinessException("上传失败：" + e.getMessage());
        }
    }


    @DeleteMapping("/delete/{id}")
    public Result deleteFurniture(@PathVariable Long id) {
        return furnitureManageService.deleteFurniture(id);
    }

}

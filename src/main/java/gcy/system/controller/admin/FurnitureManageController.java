package gcy.system.controller.admin;

import gcy.system.aspect.OperationLog;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.AdminFurnitureFormDTO;
import gcy.system.exception.BusinessException;
import gcy.system.integration.OssService;
import gcy.system.service.admin.IFurnitureManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 家具管理控制器
 * <p>
 * 提供后台管理系统中家具的增删改查及图片上传功能，
 * 所有接口均映射至 /admin/furniture 路径下。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Tag(name = "家具管理", description = "家具管理相关接口")
@RestController
@RequestMapping("/admin/furniture")
@RequiredArgsConstructor
public class FurnitureManageController {

    private final IFurnitureManageService furnitureManageService;

    private final OssService ossService;

    /**
     * 分页查询家具列表
     * <p>
     * GET /admin/furniture/list —— 支持按家具类型、名称、库存状态和品牌进行筛选，
     * 返回分页后的家具数据。
     * </p>
     *
     * @param current     当前页码，默认值为 1
     * @param size        每页显示条数，默认值为 10
     * @param typeId      家具类型 ID，可选
     * @param fName       家具名称，可选，支持模糊查询
     * @param stockStatus 库存状态，可选
     * @param brand       品牌，可选
     * @return 包含分页家具列表数据的 Result 对象
     */
    @Operation(summary = "分页查询家具列表")
    @GetMapping("/list")
    public Result getFurnitureList(@Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
                                   @Parameter(description = "每页显示条数") @RequestParam(defaultValue = "10") Integer size,
                                   @Parameter(description = "家具类型ID") @RequestParam(required = false) Long typeId,
                                   @Parameter(description = "家具名称") @RequestParam(required = false) String fName,
                                   @Parameter(description = "库存状态") @RequestParam(required = false) String stockStatus,
                                   @Parameter(description = "品牌") @RequestParam(required = false) String brand) {
        return furnitureManageService.getFurnitureList(current, size, typeId, fName, stockStatus, brand);
    }

    /**
     * 新增家具
     * <p>
     * POST /admin/furniture/add —— 接收家具表单数据并创建一条新的家具记录。
     * </p>
     *
     * @param dto 家具表单数据传输对象，包含家具的名称、类型、价格、库存等信息，需通过校验
     * @return 包含操作结果的 Result 对象
     */
    @OperationLog("新增商品")
    @Operation(summary = "新增家具")
    @PostMapping("/add")
    public Result addFurniture(@Parameter(description = "请求体") @RequestBody @Valid AdminFurnitureFormDTO dto) {
        return furnitureManageService.addFurniture(dto);
    }

    /**
     * 编辑家具
     * <p>
     * PUT /admin/furniture/edit —— 接收家具表单数据并更新已有的家具记录。
     * </p>
     *
     * @param dto 家具表单数据传输对象，包含需要更新的家具信息，需通过校验
     * @return 包含操作结果的 Result 对象
     */
    @OperationLog("编辑商品")
    @Operation(summary = "编辑家具")
    @PutMapping("/edit")
    public Result editFurniture(@Parameter(description = "请求体") @RequestBody @Valid AdminFurnitureFormDTO dto) {
        return furnitureManageService.editFurniture(dto);
    }

    /**
     * 上传家具图片
     * <p>
     * POST /admin/furniture/upload —— 将家具图片上传至 OSS，返回图片的访问 URL。
     * </p>
     *
     * @param file 要上传的图片文件
     * @return 包含上传后图片 URL 的 Result 对象
     */
    @Operation(summary = "上传家具图片")
    @PostMapping("/upload")
    public Result uploadFurnitureImage(@Parameter(description = "图片文件") @RequestParam("file") MultipartFile file) {
        try {
            String url = ossService.upload(file, "furniture");
            return Result.ok(url);
        } catch (Exception e) {
            throw new BusinessException("上传失败：" + e.getMessage());
        }
    }


    /**
     * 删除家具
     * <p>
     * DELETE /admin/furniture/delete/{id} —— 根据家具 ID 删除指定的家具记录。
     * </p>
     *
     * @param id 要删除的家具 ID
     * @return 包含操作结果的 Result 对象
     */
    @OperationLog("删除商品")
    @Operation(summary = "删除家具")
    @DeleteMapping("/delete/{id}")
    public Result deleteFurniture(@Parameter(description = "家具ID") @PathVariable Long id) {
        return furnitureManageService.deleteFurniture(id);
    }

}

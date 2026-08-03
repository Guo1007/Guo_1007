package gcy.system.controller.admin;

import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.AdminFurnitureTypeFormDTO;
import gcy.system.exception.BusinessException;
import gcy.system.integration.OssService;
import gcy.system.service.admin.IFurnitureTypeManageService;
import gcy.system.aspect.OperationLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 家具类型管理控制器
 * <p>
 * 提供家具类型的增删改查及图标上传等后台管理接口，
 * 所有接口均映射在 /admin/furniture_type 路径下。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@RestController
@Validated
@RequestMapping("/admin/furniture_type")
@RequiredArgsConstructor
public class FurnitureTypeManageController {

    private final IFurnitureTypeManageService furnitureTypeManageService;

    private final OssService ossService;

    /**
     * 新增家具类型
     * <p>
     * POST /admin/furniture_type/add
     * </p>
     *
     * @param dto 家具类型表单数据，包含类型名称、描述等信息
     * @return 操作结果，包含新增成功或失败的状态信息
     */
    @OperationLog("新增分类")
    @PostMapping("/add")
    public Result addFurnitureType(@Valid @RequestBody AdminFurnitureTypeFormDTO dto) {
        return furnitureTypeManageService.addFurnitureType(dto);
    }

    /**
     * 编辑家具类型
     * <p>
     * PUT /admin/furniture_type/update
     * </p>
     *
     * @param dto 家具类型表单数据，包含要更新的类型ID及修改后的字段信息
     * @return 操作结果，包含更新成功或失败的状态信息
     */
    @OperationLog("编辑分类")
    @PutMapping("/update")
    public Result editFurnitureType(@Valid @RequestBody AdminFurnitureTypeFormDTO dto) {
        return furnitureTypeManageService.editFurnitureType(dto);
    }

    /**
     * 上传家具类型图标
     * <p>
     * POST /admin/furniture_type/upload
     * 将图标文件上传至OSS对象存储，返回可访问的URL地址。
     * </p>
     *
     * @param file 上传的图标文件，通过表单的 file 字段提交
     * @return 操作结果，成功时 data 字段包含上传后的图标URL
     */
    @PostMapping("/upload")
    public Result uploadTypeIcon(@RequestParam("file") MultipartFile file) {
        try {
            String url = ossService.upload(file, "type");
            return Result.ok(url);
        } catch (Exception e) {
            throw new BusinessException("上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除家具类型
     * <p>
     * DELETE /admin/furniture_type/delete/{id}
     * </p>
     *
     * @param id 要删除的家具类型ID
     * @return 操作结果，包含删除成功或失败的状态信息
     */
    @OperationLog("删除分类")
    @DeleteMapping("/delete/{id}")
    public Result deleteFurnitureType(@PathVariable Long id) {
        return furnitureTypeManageService.deleteFurnitureType(id);
    }

    /**
     * 根据ID获取家具类型详细信息
     * <p>
     * GET /admin/furniture_type/info/{id}
     * </p>
     *
     * @param id 家具类型ID
     * @return 操作结果，data 字段包含该家具类型的详细信息
     */
    @GetMapping("/info/{id}")
    public Result getFurnitureTypeInfo(@PathVariable Long id) {
        return furnitureTypeManageService.getFurnitureTypeById(id);
    }

    /**
     * 分页查询家具类型列表
     * <p>
     * GET /admin/furniture_type/list
     * 支持按名称模糊筛选并分页返回结果。
     * </p>
     *
     * @param current 当前页码，默认为1
     * @param size    每页条数，默认为10
     * @param name    按家具类型名称模糊搜索的关键词，可选参数
     * @return 操作结果，data 字段包含分页后的家具类型列表及分页信息
     */
    @GetMapping("/list")
    public Result getFurnitureTypeList(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String name) {
        return furnitureTypeManageService.getFurnitureTypeList(current, size, name);
    }
}

package gcy.system.controller.admin;


import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.EditUserFormDTO;
import gcy.system.service.admin.IUserManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * <p>
 * 提供管理员对系统用户的查询、编辑、删除等管理功能。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserManageController {

    private final IUserManageService userManageService;

    /**
     * 分页获取用户列表
     * <p>
     * 支持按手机号、邮箱、管理员身份等条件进行筛选查询。
     *
     * @param current 当前页码，默认为 1
     * @param size    每页显示条数，默认为 10
     * @param phone   手机号筛选条件（可选）
     * @param email   邮箱筛选条件（可选）
     * @param isAdmin 是否为管理员筛选条件（可选）：1 表示管理员，0 表示普通用户
     * @return 包含分页用户列表数据的统一返回结果
     */
    @GetMapping("/list")
    public Result getUserList(@RequestParam(defaultValue = "1") Integer current,
                              @RequestParam(defaultValue = "10") Integer size,
                              @RequestParam(required = false) String phone,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) Integer isAdmin) {
        return userManageService.getUserList(current, size, phone, email, isAdmin);
    }

    /**
     * 编辑用户信息
     * <p>
     * 根据传入的用户表单数据更新指定用户的信息。
     *
     * @param dto 用户编辑表单数据传输对象，包含要修改的用户 ID 及新的用户信息
     * @return 表示操作结果的统一返回结果
     */
    @PutMapping("/edit")
    public Result editUser(@RequestBody EditUserFormDTO dto) {
        return userManageService.editUser(dto);
    }

    /**
     * 根据用户 ID 删除用户
     *
     * @param id 要删除的用户唯一标识 ID
     * @return 表示删除操作结果的统一返回结果
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteUser(@PathVariable Long id) {
        return userManageService.deleteUserById(id);
    }

    /**
     * 获取精简用户列表
     * <p>
     * 用于下拉选择框等场景，支持按关键字模糊匹配用户名等信息。
     *
     * @param keyword 搜索关键字（可选），用于模糊匹配用户名或其他标识信息
     * @return 包含精简用户列表数据的统一返回结果
     */
    @GetMapping("/simple")
    public Result getSimpleUserList(@RequestParam(required = false) String keyword) {
        return userManageService.getSimpleUserList(keyword);
    }

}

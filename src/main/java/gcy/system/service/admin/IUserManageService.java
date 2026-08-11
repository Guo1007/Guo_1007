package gcy.system.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.CreateUserDTO;
import gcy.system.entity.dto.admin.EditUserFormDTO;
import gcy.system.entity.dto.admin.AdminResetPasswordDTO;
import gcy.system.entity.pojo.User;

/**
 * 用户管理服务接口
 * <p>
 * 提供后台管理系统中用户相关的业务操作，包括用户新增、列表查询、
 * 用户信息编辑、用户删除以及简单用户列表搜索等功能。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface IUserManageService extends IService<User> {

    /**
     * 新增用户
     * <p>
     * 管理员创建新用户账号（普通用户/管理员），对密码加密存储，
     * 校验手机号与邮箱唯一性（userName 非唯一索引，不校验唯一）。
     * </p>
     *
     * @param dto 新增用户表单数据传输对象
     * @return 包含操作结果的结果对象
     */
    Result createUser(CreateUserDTO dto);

    /**
     * 分页查询用户列表
     * <p>
     * 根据手机号、邮箱、是否为管理员等条件进行筛选，
     * 支持分页参数控制返回的数据范围。
     * </p>
     *
     * @param current 当前页码，用于分页查询
     * @param size    每页记录数，控制每页返回的数据条数
     * @param phone   手机号筛选条件，可为空，为空时不按手机号过滤
     * @param email   邮箱筛选条件，可为空，为空时不按邮箱过滤
     * @param isAdmin 是否管理员筛选条件，可为空，为空时不按角色过滤
     * @return 包含分页用户列表数据的结果对象
     */
    Result getUserList(Integer current, Integer size,
                       String phone, String email, Integer isAdmin);

    /**
     * 编辑用户信息
     * <p>
     * 接收前端提交的用户编辑表单数据，对指定用户的信息进行更新操作。
     * </p>
     *
     * @param dto 用户编辑表单数据传输对象，包含要修改的用户ID及各项字段信息
     * @return 包含编辑操作结果的结果对象，成功或失败信息
     */
    Result editUser(EditUserFormDTO dto);

    /**
     * 重置用户密码
     * <p>
     * 管理员为指定用户设置新密码，重置后清理该用户全部登录态使其重新登录。
     * </p>
     *
     * @param dto 重置密码表单数据传输对象，包含用户ID和新密码
     * @return 包含重置操作结果的结果对象
     */
    Result resetPassword(AdminResetPasswordDTO dto);

    /**
     * 根据用户ID删除用户
     * <p>
     * 对指定的用户执行删除操作，通常为逻辑删除或物理删除，
     * 具体取决于底层实现。
     * </p>
     *
     * @param userId 要删除的用户唯一标识ID
     * @return 包含删除操作结果的结果对象，成功或失败信息
     */
    Result deleteUserById(Long userId);

    /**
     * 获取简单用户列表
     * <p>
     * 根据关键词进行模糊搜索，返回匹配的用户简要信息列表，
     * 通常用于下拉选择框或快速查找场景。
     * </p>
     *
     * @param keyword 搜索关键词，用于按用户名、手机号等字段进行模糊匹配
     * @return 包含匹配用户简要信息列表的结果对象
     */
    Result getSimpleUserList(String keyword);

}

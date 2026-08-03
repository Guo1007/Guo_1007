package gcy.system.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import gcy.system.entity.dto.*;
import gcy.system.service.IUserService;
import gcy.system.integration.OssService;
import gcy.system.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户控制器，处理与用户相关的HTTP请求，包括注册、登录、登出、密码管理、信息更新和头像上传等功能。
 * <p>
 * 所有接口均以 "/user" 为根路径。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Tag(name = "用户", description = "用户相关接口")
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    private final OssService ossService;

    /**
     * 发送注册验证码。
     * <p>
     * POST /user/r_code
     *
     * @param registerFormDTO 注册表单数据，包含用户注册所需信息（如手机号等）
     * @return 包含发送结果的 {@link Result} 对象
     */
    @Operation(summary = "发送注册验证码")
    @PostMapping("/r_code")
    public Result sendRegisterCode(@Parameter(description = "请求体") @RequestBody RegisterFormDTO registerFormDTO) {
        return userService.sendRegisterCode(registerFormDTO);
    }

    /**
     * 发送登录验证码。
     * <p>
     * POST /user/code
     *
     * @param loginFormDTO 登录表单数据，包含用户登录所需信息（如手机号等）
     * @return 包含发送结果的 {@link Result} 对象
     */
    @Operation(summary = "发送登录验证码")
    @PostMapping("/code")
    public Result sendLoginCode(@Parameter(description = "请求体") @RequestBody LoginFormDTO loginFormDTO) {
        return userService.sendLoginCode(loginFormDTO);
    }

    /**
     * 发送重置密码验证码。
     * <p>
     * POST /user/reset-code
     *
     * @param dto 重置密码表单数据，包含用户标识信息（如手机号等）
     * @return 包含发送结果的 {@link Result} 对象
     */
    @Operation(summary = "发送重置密码验证码")
    @PostMapping("/reset-code")
    public Result sendResetCode(@Parameter(description = "请求体") @RequestBody ResetPasswordFormDTO dto) {
        return userService.sendResetCode(dto);
    }

    /**
     * 重置密码。
     * <p>
     * POST /user/reset-password
     *
     * @param dto 重置密码表单数据，包含新密码及验证码等必要信息
     * @return 包含操作结果的 {@link Result} 对象
     */
    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public Result resetPassword(@Parameter(description = "请求体") @RequestBody ResetPasswordFormDTO dto) {
        return userService.resetPassword(dto);
    }

    /**
     * 用户登录。
     * <p>
     * POST /user/login
     *
     * @param loginFormDTO 登录表单数据，包含登录凭证（如手机号、验证码或密码等）
     * @return 包含登录结果的 {@link Result} 对象，登录成功时通常包含用户信息和令牌
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result login(@Parameter(description = "请求体") @RequestBody LoginFormDTO loginFormDTO) {
        return userService.login(loginFormDTO);
    }

    /**
     * 用户登出。
     * <p>
     * POST /user/logout
     *
     * @return 包含登出结果的 {@link Result} 对象
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result logout() {
        return userService.logout();
    }

    /**
     * 用户注册。
     * <p>
     * POST /user/register
     *
     * @param registerFormDTO 注册表单数据，包含用户名、密码、手机号等注册所需信息
     * @return 包含注册结果的 {@link Result} 对象
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result register(@Parameter(description = "请求体") @RequestBody RegisterFormDTO registerFormDTO) {
        return userService.register(registerFormDTO);
    }

    /**
     * 获取当前登录用户的信息。
     * <p>
     * GET /user/me
     *
     * @return 包含当前用户信息的 {@link Result} 对象，其中包含用户基本信息和是否有密码的标志
     */
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result me() {
        UserDTO user = UserHolder.getUser();
        UserDTO copy = BeanUtil.copyProperties(user, UserDTO.class);
        copy.setHasPassword(StrUtil.isNotBlank(user.getPassWord()));
        return Result.ok(copy);
    }

    /**
     * 修改当前登录用户的密码。
     * <p>
     * PUT /user/password
     *
     * @param dto 密码修改表单数据，包含旧密码和新密码
     * @return 包含操作结果的 {@link Result} 对象
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result updatePassword(@Parameter(description = "请求体") @RequestBody PasswordFormDTO dto) {
        return userService.updatePassword(dto);
    }

    /**
     * 更新当前登录用户的个人信息。
     * <p>
     * PUT /user/update
     *
     * @param dto 用户信息更新表单数据，包含需要修改的用户字段（如昵称、头像等）
     * @return 包含操作结果的 {@link Result} 对象
     */
    @Operation(summary = "更新个人信息")
    @PutMapping("/update")
    public Result updateUser(@Parameter(description = "请求体") @RequestBody UpdateFormDTO dto) {
        return userService.updateUser(dto);
    }

    /**
     * 上传用户头像。
     * <p>
     * POST /user/upload/avatar
     *
     * @param file 用户上传的头像文件，通过表单字段 "file" 提交
     * @return 包含头像文件访问路径的 {@link Result} 对象
     */
    @Operation(summary = "上传用户头像")
    @PostMapping("/upload/avatar")
    public Result uploadAvatar(@Parameter(description = "头像文件") @RequestParam("file") MultipartFile file) {
        String path = ossService.uploadAvatar(file);
        return Result.ok(path);
    }

}

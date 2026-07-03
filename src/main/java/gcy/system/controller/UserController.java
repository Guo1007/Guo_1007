package gcy.system.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import gcy.system.entity.dto.*;
import gcy.system.service.IUserService;
import gcy.system.service.OssService;
import gcy.system.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    private final OssService ossService;

    @PostMapping("/r_code")
    public Result sendRegisterCode(@RequestBody RegisterFormDTO registerFormDTO) {
        return userService.sendRegisterCode(registerFormDTO);
    }

    @PostMapping("/code")
    public Result sendLoginCode(@RequestBody LoginFormDTO loginFormDTO) {
        return userService.sendLoginCode(loginFormDTO);
    }

    @PostMapping("/reset-code")
    public Result sendResetCode(@RequestBody ResetPasswordFormDTO dto) {
        return userService.sendResetCode(dto);
    }

    @PostMapping("/reset-password")
    public Result resetPassword(@RequestBody ResetPasswordFormDTO dto) {
        return userService.resetPassword(dto);
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginFormDTO) {
        return userService.login(loginFormDTO);
    }

    @PostMapping("/logout")
    public Result logout() {
        return userService.logout();
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterFormDTO registerFormDTO) {
        return userService.register(registerFormDTO);
    }

    @GetMapping("/me")
    public Result me() {
        UserDTO user = UserHolder.getUser();
        UserDTO copy = BeanUtil.copyProperties(user, UserDTO.class);
        copy.setHasPassword(StrUtil.isNotBlank(user.getPassWord()));
        return Result.ok(copy);
    }

    @PutMapping("/password")
    public Result updatePassword(@RequestBody PasswordFormDTO dto) {
        return userService.updatePassword(dto);
    }

    @PutMapping("/update")
    public Result updateUser(@RequestBody UpdateFormDTO dto) {
        return userService.updateUser(dto);
    }

    @PostMapping("/upload/avatar")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file) {
        String path = ossService.uploadAvatar(file);
        return Result.ok(path);
    }

}

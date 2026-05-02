package com.example.furnituresystem.controller;


import cn.hutool.core.util.StrUtil;
import com.example.furnituresystem.entity.dto.*;
import com.example.furnituresystem.service.IUserService;
import com.example.furnituresystem.utils.FileUploadUtil;
import com.example.furnituresystem.utils.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private FileUploadUtil fileUploadUtil;

    @PostMapping("/r_code")
    public Result sendRegisterCode(@RequestBody RegisterFormDTO registerFormDTO) {
        return userService.sendRegisterCode(registerFormDTO);
    }

    @PostMapping("/code")
    public Result sendLoginCode(@RequestBody LoginFormDTO loginFormDTO) {
        return userService.sendLoginCode(loginFormDTO);
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
        user.setHasPassword(StrUtil.isNotBlank(user.getPassWord()));
        return Result.ok(user);
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
        String path = FileUploadUtil.uploadAvatar(file);
        return Result.ok(path);
    }

}

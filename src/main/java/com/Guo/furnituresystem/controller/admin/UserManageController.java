package com.Guo.furnituresystem.controller.admin;


import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.entity.dto.admin.EditUserFormDTO;
import com.Guo.furnituresystem.service.admin.IUserManageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/admin/user")
public class UserManageController {

    @Resource
    private IUserManageService userManageService;

    @GetMapping("/list")
    public Result getUserList(@RequestParam(defaultValue = "1") Integer current,
                              @RequestParam(defaultValue = "10") Integer size,
                              @RequestParam(required = false) String phone,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) Integer isAdmin) {
        return userManageService.getUserList(current, size, phone, email, isAdmin);
    }

    @PutMapping("/edit")
    public Result editUser(@RequestBody EditUserFormDTO dto) {
        return userManageService.editUser(dto);
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteUser(@PathVariable Long id) {
        return userManageService.deleteUserById(id);
    }

    @GetMapping("/simple")
    public Result getSimpleUserList(@RequestParam(required = false) String keyword) {
        return userManageService.getSimpleUserList(keyword);
    }

}

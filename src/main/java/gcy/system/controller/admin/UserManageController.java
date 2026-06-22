package gcy.system.controller.admin;


import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.EditUserFormDTO;
import gcy.system.service.admin.IUserManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserManageController {

    private final IUserManageService userManageService;

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

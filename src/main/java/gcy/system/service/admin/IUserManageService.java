package gcy.system.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.EditUserFormDTO;
import gcy.system.entity.pojo.User;


public interface IUserManageService extends IService<User> {

    Result getUserList(Integer current, Integer size,
                       String phone, String email, Integer isAdmin);

    Result editUser(EditUserFormDTO dto);

    Result deleteUserById(Long userId);

    Result getSimpleUserList(String keyword);

}

package com.Guo.furnituresystem.service.admin;

import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.entity.dto.admin.EditUserFormDTO;
import com.Guo.furnituresystem.entity.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IUserManageService extends IService<User> {

    Result getUserList(Integer current, Integer size,
                       String phone, String email, Integer isAdmin);

    Result editUser(EditUserFormDTO dto);

    Result deleteUserById(Long userId);

    Result getSimpleUserList(String keyword);

}

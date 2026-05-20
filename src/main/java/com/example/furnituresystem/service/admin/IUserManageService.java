package com.example.furnituresystem.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.dto.admin.EditUserFormDTO;
import com.example.furnituresystem.entity.pojo.User;


public interface IUserManageService extends IService<User> {

    Result getUserList(Integer current, Integer size,
                       String phone, String email, Integer isAdmin);

    Result editUser(EditUserFormDTO dto);

    Result deleteUserById(Long userId);

    Result getSimpleUserList(String keyword);

}

package com.example.furnituresystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.furnituresystem.entity.dto.*;
import com.example.furnituresystem.entity.pojo.User;

public interface IUserService extends IService<User> {

    Result sendRegisterCode(RegisterFormDTO registerFormDTO);

    Result sendLoginCode(LoginFormDTO loginFormDTO);

    Result login(LoginFormDTO loginFormDTO);

    Result logout();

    Result register(RegisterFormDTO registerFormDTO);

    Result updatePassword(PasswordFormDTO dto);

    Result updateUser(UpdateFormDTO updateFormDTO);


}

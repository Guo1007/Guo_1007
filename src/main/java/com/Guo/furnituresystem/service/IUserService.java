package com.Guo.furnituresystem.service;

import com.Guo.furnituresystem.entity.dto.*;
import com.Guo.furnituresystem.entity.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IUserService extends IService<User> {

    Result sendRegisterCode(RegisterFormDTO registerFormDTO);

    Result sendLoginCode(LoginFormDTO loginFormDTO);

    Result login(LoginFormDTO loginFormDTO);

    Result logout();

    Result register(RegisterFormDTO registerFormDTO);

    Result updatePassword(PasswordFormDTO dto);

    Result updateUser(UpdateFormDTO updateFormDTO);


}

package gcy.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.*;
import gcy.system.entity.pojo.User;

public interface IUserService extends IService<User> {

    Result sendRegisterCode(RegisterFormDTO registerFormDTO);

    Result sendLoginCode(LoginFormDTO loginFormDTO);

    Result login(LoginFormDTO loginFormDTO);

    Result logout();

    Result register(RegisterFormDTO registerFormDTO);

    Result updatePassword(PasswordFormDTO dto);

    Result updateUser(UpdateFormDTO updateFormDTO);


}

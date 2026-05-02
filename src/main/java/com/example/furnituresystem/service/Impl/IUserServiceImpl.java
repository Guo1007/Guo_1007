package com.example.furnituresystem.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.furnituresystem.entity.dto.*;
import com.example.furnituresystem.entity.pojo.User;
import com.example.furnituresystem.exception.BusinessException;
import com.example.furnituresystem.mapper.UserMapper;
import com.example.furnituresystem.service.IUserService;
import com.example.furnituresystem.utils.PasswordUtil;
import com.example.furnituresystem.utils.RegexUtils;
import com.example.furnituresystem.utils.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.furnituresystem.utils.RedisConstants.*;
import static com.example.furnituresystem.utils.SystemConstants.USER_NAME_PREFIX;

@Slf4j
@Service
public class IUserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private enum CodeType {
        LOGIN(LOGIN_CODE_KEY),
        REGISTER(REGISTER_CODE_KEY);

        private final String keyPrefix;

        CodeType(String prefix) {
            this.keyPrefix = prefix;
        }

        public String getKey(String phone) {
            return keyPrefix + phone;
        }

    }

    private Result sendCode(String phone, CodeType type) {
        Assert.isTrue(!RegexUtils.isPhoneInvalid(phone), "手机号格式有误！");
        String existingCode = stringRedisTemplate.opsForValue().get(type.getKey(phone));
        Assert.isNull(existingCode, "操作过于频繁，请稍后再试");
        String code = RandomUtil.randomNumbers(6);
        log.debug("{}验证码发送成功：{}", type.name(), code);
        stringRedisTemplate.opsForValue().set(type.getKey(phone), code,
                type == CodeType.LOGIN ? LOGIN_CODE_TTL : REGISTER_CODE_TTL,
                TimeUnit.MINUTES);
        return Result.ok();
    }

    @Override
    public Result sendRegisterCode(RegisterFormDTO dto) {
        return sendCode(dto.getPhone(), CodeType.REGISTER);
    }

    @Override
    public Result sendLoginCode(LoginFormDTO dto) {
        return sendCode(dto.getPhone(), CodeType.LOGIN);
    }

    @Override
    public Result login(LoginFormDTO loginFormDTO) {
        Assert.notNull(loginFormDTO, "请求参数不能为空");
        String phone = loginFormDTO.getPhone();
        String code = loginFormDTO.getCode();
        String passWord = loginFormDTO.getPassWord();
        if (StrUtil.isNotBlank(code)) {
            return loginByCode(phone, code);
        } else if (StrUtil.isNotBlank(passWord)) {
            return loginByPwd(phone, passWord);
        } else {
            throw new IllegalArgumentException("请输入验证码或密码");
        }
    }

    @Override
    public Result logout() {
        String token = UserHolder.getToken();
        if (StrUtil.isNotBlank(token)) {
            String redisKey = LOGIN_USER_KEY + token;
            Boolean isDeleted = stringRedisTemplate.delete(redisKey);
            if (isDeleted) {
                log.info("用户退出登录成功，Token: {}, Redis已清除", token);
            } else {
                log.warn("用户退出登录，但 Redis 中未找到该 Token: {}", token);
            }
        }
        UserHolder.removeUser();
        return Result.ok();
    }

    @Override
    public Result register(RegisterFormDTO registerFormDTO) {
        Assert.notNull(registerFormDTO, "请输入完整信息！");
        String phone = registerFormDTO.getPhone();
        String password = registerFormDTO.getPassword();
        String confirmPwd = registerFormDTO.getConfirmPwd();
        Assert.isTrue(!RegexUtils.isPasswordInvalid(password), "密码格式错误！");
        Assert.isTrue(!RegexUtils.isPasswordInvalid(confirmPwd), "确认密码格式错误！");
        Assert.isTrue(!RegexUtils.isPhoneInvalid(phone), "手机号格式有误！");
        Assert.isTrue(!StrUtil.isBlank(password), "密码不能为空！");
        Assert.isTrue(!StrUtil.isBlank(confirmPwd), "确认密码不能为空！");
        Assert.isTrue(password.equals(confirmPwd), "两次密码不一致！");
        User existingUser = query().eq("phone", phone).one();
        Assert.isNull(existingUser, "该手机号已被注册，请直接登录");
        String nickName = RandomUtil.randomString(10);
        String userName = USER_NAME_PREFIX + nickName;
        User user = new User();
        user.setPhone(phone);
        user.setUserName(userName);
        user.setPassWord(PasswordUtil.encode(password));
        user.setCreateTime(LocalDateTime.now());
        save(user);
        stringRedisTemplate.delete(CodeType.REGISTER.getKey(phone));
        return Result.ok("注册成功");
    }

    @Override
    @Transactional
    public Result updatePassword(PasswordFormDTO dto) {
        UserDTO userDTO = UserHolder.getUser();
        String token = UserHolder.getToken();
        String newPassword = dto.getNewPassword();
        String confirmPassword = dto.getConfirmPassword();
        if (!(newPassword.equals(confirmPassword))) {
            throw new BusinessException("两次密码输入不一致！");
        }
        Assert.isTrue(!RegexUtils.isPasswordInvalid(dto.getNewPassword()), "新密码格式错误！");
        Assert.isTrue(!RegexUtils.isPasswordInvalid(confirmPassword), "确认密码格式错误！");
        String oldPassword = dto.getOldPassword();
        String dbPassword = userDTO.getPassWord();
        if (StrUtil.isNotBlank(oldPassword)) {
            if (StrUtil.isBlank(dbPassword)) {
                throw new BusinessException("该账户未设置密码，无需输入旧密码！");
            }
            if (!PasswordUtil.matches(oldPassword, dbPassword)) {
                throw new BusinessException("旧密码输入错误！");
            }
        } else {
            if (StrUtil.isNotBlank(dbPassword)) {
                throw new BusinessException("请输入旧密码！");
            }
        }
        String password = PasswordUtil.encode(dto.getNewPassword());
        userDTO.setPassWord(password);
        User user = BeanUtil.copyProperties(userDTO, User.class);
        boolean success = updateById(user);
        if (!success) {
            throw new BusinessException("设置失败，请稍后重试或反馈！");
        }
        String cacheKey = LOGIN_USER_KEY + token;
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> {
                            if (fieldValue == null) {
                                return null;
                            }
                            return fieldValue.toString();
                        }));
        stringRedisTemplate.opsForHash().putAll(cacheKey, userMap);
        stringRedisTemplate.expire(cacheKey, LOGIN_USER_TTL, TimeUnit.SECONDS);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result updateUser(UpdateFormDTO updateFormDTO) {
        Long userId = UserHolder.getUser().getId();
        String token = UserHolder.getToken();
        if (userId == null) {
            throw new BusinessException("请检查登录状态，可尝试重新登录！");
        }
        User user = new User();
        user.setId(userId);
        BeanUtil.copyProperties(updateFormDTO, user, CopyOptions.create()
                .setIgnoreNullValue(true));
        boolean success = updateById(user);
        if (!success) {
            throw new BusinessException("更新失败，请尝试重启系统！");
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> {
                            if (fieldValue == null) {
                                return null;
                            }
                            return fieldValue.toString();
                        }));
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, userMap);
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.SECONDS);
        return Result.ok();
    }

    private Result getAndReturnToken(User user) {
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> {
                            if (fieldValue == null) {
                                return null;
                            }
                            return fieldValue.toString();
                        }));
        String token = UUID.randomUUID(true).toString();
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, userMap);
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.SECONDS);
        return Result.ok(token);
    }

    private Result loginByCode(String phone, String code) {
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        Assert.isTrue(!StrUtil.isBlank(cacheCode), "验证码已过期或未发送");
        Assert.isTrue(code.equals(cacheCode), "验证码错误");
        stringRedisTemplate.delete(LOGIN_CODE_KEY + phone);
        User user = query().eq("phone", phone).one();
        if (user == null) {
            user = createUserWithPhone(phone);
        }

        return getAndReturnToken(user);
    }

    public Result loginByPwd(String phone, String password) {
        Assert.isTrue(!RegexUtils.isPhoneInvalid(phone), "手机号格式有误！");
        User user = query().eq("phone", phone).one();
        Assert.notNull(user, "用户不存在，请先注册");
        Assert.isTrue(!StrUtil.isBlank(user.getPassWord()), "该用户密码为空，请使用短信验证码登录后设置密码！");
        Assert.isTrue(PasswordUtil.matches(password, user.getPassWord()), "密码错误");
        return getAndReturnToken(user);
    }

    private User createUserWithPhone(String phone) {
        String nickName = RandomUtil.randomString(10);
        String userName = USER_NAME_PREFIX + nickName;
        User user = new User();
        user.setPhone(phone);
        user.setUserName(userName);
        user.setCreateTime(LocalDateTime.now());
        save(user);
        return user;
    }

}
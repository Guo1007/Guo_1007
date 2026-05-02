package com.example.furnituresystem.service.admin.Impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.dto.admin.EditUserFormDTO;
import com.example.furnituresystem.entity.pojo.User;
import com.example.furnituresystem.entity.vo.UserVO;
import com.example.furnituresystem.exception.BusinessException;
import com.example.furnituresystem.mapper.admin.UserManageMapper;
import com.example.furnituresystem.service.admin.IUserManageService;
import com.example.furnituresystem.utils.PasswordUtil;
import com.example.furnituresystem.utils.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.furnituresystem.utils.RedisConstants.LOGIN_USER_KEY;

@Service
@Slf4j
public class IUserManageServiceImpl extends ServiceImpl<UserManageMapper, User>
        implements IUserManageService {

    @Resource
    private UserManageMapper userManageMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result getUserList(Integer current, Integer size, String phone, Integer isAdmin) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(phone)) {
            wrapper.like(User::getPhone, phone);
        }
        if (isAdmin != null) {
            wrapper.eq(User::getIsAdmin, isAdmin);
        }
        wrapper.orderByAsc(User::getCreateTime);
        Page<User> userPage = userManageMapper.selectPage(page, wrapper);
        List<UserVO> voList = new ArrayList<>();
        for (User user : userPage.getRecords()) {
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUserName(user.getUserName());
            vo.setPhone(user.getPhone());
            vo.setIsAdmin(user.getIsAdmin());
            vo.setAddress(user.getAddress());
            vo.setCreateTime(user.getCreateTime());
            voList.add(vo);
        }
        Page<UserVO> resultPage = new Page<>();
        resultPage.setCurrent(userPage.getCurrent());
        resultPage.setSize(userPage.getSize());
        resultPage.setTotal(userPage.getTotal());
        resultPage.setRecords(voList);
        return Result.ok(resultPage);

    }

    @Override
    @Transactional
    public Result editUser(EditUserFormDTO dto) {
        if (dto == null) {
            return Result.fail("请完善修改信息！");
        }
        if (dto.getNewPassword() == null) {
            return Result.fail("请输入重置后的密码！");
        }
        if (dto.getIsAdmin() == null) {
            return Result.fail("请选择用户身份！");
        }
        User user = getById(dto.getId());
        if (user == null) {
            return Result.fail("用户不存在！");
        }
        user.setIsAdmin(dto.getIsAdmin());
        if (StrUtil.isNotBlank(dto.getNewPassword())) {
            String encryptedPwd = PasswordUtil.encode(dto.getNewPassword());
            user.setPassWord(encryptedPwd);
        }
        boolean success = updateById(user);
        if (!success) {
            throw new BusinessException("修改用户失败，请稍后重试！");
        }
        return Result.ok("修改成功");
    }

    @Override
    @Transactional
    public Result deleteUserById(Long userId) {
        User user = getById(userId);
        if (user == null) {
            return Result.fail("用户不存在！");
        }
        Long id = UserHolder.getUser().getId();
        if (Objects.equals(id, userId)) {
            return Result.fail("请勿删除自己！");
        }
        boolean success = removeById(userId);
        if (!success) {
            throw new BusinessException("删除用户失败，请稍后重试！");
        }
        String userJson = stringRedisTemplate.opsForValue().get(LOGIN_USER_KEY + userId);
        if (StrUtil.isNotBlank(userJson)) {
            stringRedisTemplate.delete(LOGIN_USER_KEY + userId);
            log.info("用户 [{}] 被删除，已清理 Redis 登录态", userId);
        }
        return Result.ok("删除成功");
    }

}

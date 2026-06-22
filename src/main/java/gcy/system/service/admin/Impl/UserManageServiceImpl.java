package gcy.system.service.admin.Impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.UserSimpleDTO;
import gcy.system.entity.dto.admin.EditUserFormDTO;
import gcy.system.entity.pojo.User;
import gcy.system.entity.vo.UserVO;
import gcy.system.exception.BusinessException;
import gcy.system.mapper.admin.UserManageMapper;
import gcy.system.service.admin.IUserManageService;
import gcy.system.utils.PasswordUtil;
import gcy.system.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static gcy.system.utils.RedisConstants.LOGIN_USER_KEY;
import static gcy.system.utils.RedisConstants.LOGIN_USER_TOKEN_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserManageServiceImpl extends ServiceImpl<UserManageMapper, User>
        implements IUserManageService {

    private final UserManageMapper userManageMapper;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Result getUserList(Integer current, Integer size, String phone, String email, Integer isAdmin) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(phone)) {
            wrapper.like(User::getPhone, phone);
        }
        if (StrUtil.isNotBlank(email)) {
            wrapper.like(User::getEmail, email);
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
            vo.setEmail(user.getEmail());
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
        String tokenKey = LOGIN_USER_TOKEN_KEY + dto.getId();
        String token = stringRedisTemplate.opsForValue().get(tokenKey);
        if (StrUtil.isNotBlank(token)) {
            stringRedisTemplate.delete(LOGIN_USER_KEY + token);
            stringRedisTemplate.delete(tokenKey);
            log.warn("用户 [{}] 信息被管理员修改，已清理最新登录态（可能存在其他设备的旧token仍有效）", dto.getId());
        }

        return Result.ok("修改成功，用户需重新登录");
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
        String tokenKey = LOGIN_USER_TOKEN_KEY + userId;
        String token = stringRedisTemplate.opsForValue().get(tokenKey);
        if (StrUtil.isNotBlank(token)) {
            stringRedisTemplate.delete(LOGIN_USER_KEY + token);
            stringRedisTemplate.delete(tokenKey);
            log.info("用户 [{}] 被删除，已清理 Redis 登录态", userId);
        }
        return Result.ok("删除成功");
    }

    @Override
    public Result getSimpleUserList(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(User::getId, User::getUserName, User::getEmail);
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUserName, keyword)
                    .or().like(User::getEmail, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        wrapper.last("LIMIT 200");
        List<User> users = userManageMapper.selectList(wrapper);
        List<UserSimpleDTO> list = users.stream()
                .map(u -> new UserSimpleDTO(u.getId(), u.getUserName(), u.getEmail()))
                .collect(java.util.stream.Collectors.toList());
        return Result.ok(list);
    }

}

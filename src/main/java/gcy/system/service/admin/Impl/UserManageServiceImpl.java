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
import gcy.system.mapper.UserMapper;
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

/**
 * 用户管理服务实现类，负责用户的增删改查等管理操作。
 * 继承 MyBatis-Plus 的 ServiceImpl，提供分页查询、编辑、删除及简易列表查询等功能，
 * 并在编辑或删除用户时同步清理 Redis 中的登录态缓存。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserManageServiceImpl extends ServiceImpl<UserMapper, User>
        implements IUserManageService {

    private final UserMapper userMapper;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 分页查询用户列表，支持按手机号、邮箱模糊搜索及按管理员身份精确筛选。
     * 将数据库实体转换为前端展示对象 UserVO 后返回分页结果。
     *
     * @param current 当前页码
     * @param size    每页记录数
     * @param phone   手机号搜索关键字（模糊匹配），可为空
     * @param email   邮箱搜索关键字（模糊匹配），可为空
     * @param isAdmin 是否管理员标识（0:普通用户, 1:管理员），可为空
     * @return 包含分页用户列表的 Result 对象
     */
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
        Page<User> userPage = userMapper.selectPage(page, wrapper);
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

    /**
     * 编辑用户信息，包括修改管理员身份和重置密码。
     * 执行更新后，会清理该用户在 Redis 中的最新登录态缓存，使其重新登录生效。
     *
     * @param dto 编辑用户表单数据，包含用户ID、新密码、管理员标识等
     * @return 包含操作结果提示的 Result 对象
     * @throws BusinessException 当用户更新失败时抛出业务异常
     */
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

        return Result.okMsg("修改成功，用户需重新登录");
    }

    /**
     * 根据用户ID删除用户。执行前会校验用户是否存在、是否是当前登录用户自身。
     * 删除成功后，同步清理该用户在 Redis 中的登录态缓存。
     *
     * @param userId 待删除的用户ID
     * @return 包含操作结果提示的 Result 对象
     * @throws BusinessException 当删除操作失败时抛出业务异常
     */
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
        return Result.okMsg("删除成功");
    }

    /**
     * 获取简易用户列表，支持按用户名或邮箱关键字模糊搜索，最多返回200条记录。
     * 适用于下拉选择等仅需展示用户基本信息的场景。
     *
     * @param keyword 搜索关键字（模糊匹配用户名或邮箱），可为空
     * @return 包含简易用户信息列表的 Result 对象
     */
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
        List<User> users = userMapper.selectList(wrapper);
        List<UserSimpleDTO> list = users.stream()
                .map(u -> new UserSimpleDTO(u.getId(), u.getUserName(), u.getEmail()))
                .collect(java.util.stream.Collectors.toList());
        return Result.ok(list);
    }

}

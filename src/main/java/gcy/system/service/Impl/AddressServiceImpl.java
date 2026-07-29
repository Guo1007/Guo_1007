package gcy.system.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.UserDTO;
import gcy.system.entity.pojo.UserAddress;
import gcy.system.mapper.UserAddressMapper;
import gcy.system.service.IAddressService;
import gcy.system.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户收货地址服务实现类
 * <p>
 * 基于MyBatis-Plus实现用户收货地址的增删改查、默认地址管理等业务逻辑。
 * 所有写操作均校验当前登录用户对目标地址的操作权限，确保数据安全。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements IAddressService {

    private final UserAddressMapper addressMapper;

    /**
     * 获取当前用户的收货地址列表
     * <p>
     * 通过LambdaQueryWrapper构建查询条件，按用户ID精确匹配，
     * 并优先展示默认地址，其次按创建时间倒序排列。
     * </p>
     *
     * @param userId 用户唯一标识，用于筛选该用户下的全部地址
     * @return 包含地址列表的操作结果，data字段为按默认优先、时间倒序排列的地址集合
     */
    @Override
    public Result getAddressList(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime);
        return Result.ok(addressMapper.selectList(wrapper));
    }

    /**
     * 新增或更新用户收货地址
     * <p>
     * 首先校验当前登录用户与传入userId是否一致，防止越权操作。
     * 若地址ID已存在则执行更新（更新前校验地址归属），否则执行新增。
     * 新增时若该用户尚无任何地址，自动设为首个默认地址。
     * 若保存后该地址被标记为默认，则清除该用户其他地址的默认标记。
     * </p>
     *
     * @param addr   用户地址实体，包含收货人、联系电话、详细地址及默认标记等字段
     * @param userId 当前操作用户的唯一标识，用于绑定地址归属及权限校验
     * @return 操作结果，成功时返回空data，失败时携带错误描述（无权操作、地址不存在等）
     */
    @Override
    @Transactional
    public Result saveAddress(UserAddress addr, Long userId) {
        UserDTO currentUser = UserHolder.getUser();
        if (!currentUser.getId().equals(userId)) {
            return Result.fail("无权操作");
        }
        addr.setUserId(userId);
        if (addr.getIsDefault() == null) {
            addr.setIsDefault(0);
        }
        if (addr.getId() != null) {
            // 更新前校验地址归属
            UserAddress existing = addressMapper.selectById(addr.getId());
            if (existing == null) {
                return Result.fail("地址不存在");
            }
            if (!existing.getUserId().equals(currentUser.getId())) {
                return Result.fail("无权修改该地址");
            }
            addressMapper.updateById(addr);
        } else {
            long count = addressMapper.selectCount(
                    new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId)
            );
            if (count == 0) {
                addr.setIsDefault(1);
            }
            addressMapper.insert(addr);
        }
        if (addr.getIsDefault() == 1) {
            addressMapper.clearDefaultExcept(userId, addr.getId());
        }
        return Result.ok();
    }

    /**
     * 删除指定收货地址
     * <p>
     * 通过地址ID查询目标地址，校验其存在性及归属权限（仅允许删除自己的地址），
     * 校验通过后执行物理删除。
     * </p>
     *
     * @param id 地址唯一标识，指向待删除的地址记录
     * @return 操作结果，成功时表明删除完成，失败时携带错误描述（地址不存在、无权删除）
     */
    @Override
    public Result deleteAddress(Long id) {
        UserDTO currentUser = UserHolder.getUser();
        UserAddress addr = addressMapper.selectById(id);
        if (addr == null) {
            return Result.fail("地址不存在");
        }
        if (!addr.getUserId().equals(currentUser.getId())) {
            return Result.fail("无权删除该地址");
        }
        addressMapper.deleteById(id);
        return Result.ok();
    }

    /**
     * 设置默认收货地址
     * <p>
     * 首先校验当前登录用户与传入userId是否一致，再校验目标地址是否存在且归属于当前用户。
     * 通过后先清除该用户所有地址的默认标记，再将目标地址标记为默认，
     * 两项操作在同一事务中执行以保证数据一致性。
     * </p>
     *
     * @param id     地址唯一标识，指向要设为默认的地址记录
     * @param userId 当前操作用户的唯一标识，用于校验地址归属权限
     * @return 操作结果，成功时表明默认地址已更新，失败时携带错误描述（无权操作、地址不存在）
     */
    @Override
    @Transactional
    public Result setDefaultAddress(Long id, Long userId) {
        UserDTO currentUser = UserHolder.getUser();
        if (!currentUser.getId().equals(userId)) {
            return Result.fail("无权操作");
        }
        UserAddress addr = addressMapper.selectById(id);
        if (addr == null || !addr.getUserId().equals(currentUser.getId())) {
            return Result.fail("地址不存在或无权操作");
        }
        addressMapper.clearDefault(userId);
        UserAddress updateAddr = new UserAddress();
        updateAddr.setId(id);
        updateAddr.setIsDefault(1);
        addressMapper.updateById(updateAddr);
        return Result.ok();
    }
}

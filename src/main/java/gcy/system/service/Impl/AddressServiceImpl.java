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

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements IAddressService {

    private final UserAddressMapper addressMapper;

    @Override
    public Result getAddressList(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime);
        return Result.ok(addressMapper.selectList(wrapper));
    }

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

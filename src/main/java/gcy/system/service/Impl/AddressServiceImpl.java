package gcy.system.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.UserAddress;
import gcy.system.mapper.UserAddressMapper;
import gcy.system.service.IAddressService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements IAddressService {

    @Resource
    private UserAddressMapper addressMapper;

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
        addr.setUserId(userId);
        if (addr.getIsDefault() == null) {
            addr.setIsDefault(0);
        }
        if (addr.getId() != null) {
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
        addressMapper.deleteById(id);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result setDefaultAddress(Long id, Long userId) {
        addressMapper.clearDefault(userId);
        UserAddress addr = new UserAddress();
        addr.setId(id);
        addr.setIsDefault(1);
        addressMapper.updateById(addr);
        return Result.ok();
    }
}

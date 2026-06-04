package com.Guo.furnituresystem.controller;

import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.entity.pojo.UserAddress;
import com.Guo.furnituresystem.mapper.UserAddressMapper;
import com.Guo.furnituresystem.utils.UserHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Resource
    private UserAddressMapper addressMapper;

    @GetMapping("/list")
    public Result list() {
        Long userId = UserHolder.getUser().getId();
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime);
        return Result.ok(addressMapper.selectList(wrapper));
    }

    @PostMapping("/save")
    @Transactional
    public Result save(@RequestBody UserAddress addr) {
        Long userId = UserHolder.getUser().getId();
        addr.setUserId(userId);
        if (addr.getIsDefault() == null) addr.setIsDefault(0);
        if (addr.getId() != null) {
            addressMapper.updateById(addr);
        } else {
            long count = addressMapper.selectCount(
                    new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId)
            );
            if (count == 0) addr.setIsDefault(1);
            addressMapper.insert(addr);
        }
        if (addr.getIsDefault() == 1) {
            addressMapper.clearDefaultExcept(userId, addr.getId());
        }
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        addressMapper.deleteById(id);
        return Result.ok();
    }

    @PutMapping("/default/{id}")
    @Transactional
    public Result setDefault(@PathVariable Long id) {
        Long userId = UserHolder.getUser().getId();
        addressMapper.clearDefault(userId);
        UserAddress addr = new UserAddress();
        addr.setId(id);
        addr.setIsDefault(1);
        addressMapper.updateById(addr);
        return Result.ok();
    }
}

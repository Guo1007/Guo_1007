package com.example.furnituresystem.service.admin.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.dto.admin.AdminFurnitureTypeFormDTO;
import com.example.furnituresystem.entity.pojo.FurnitureType;
import com.example.furnituresystem.mapper.admin.FurnitureTypeManageMapper;
import com.example.furnituresystem.service.admin.IFurnitureTypeManageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FurnitureTypeManageServiceImpl extends ServiceImpl<FurnitureTypeManageMapper, FurnitureType> implements IFurnitureTypeManageService {


    @Override
    @Transactional
    public Result addFurnitureType(AdminFurnitureTypeFormDTO dto) {
        FurnitureType type = BeanUtil.toBean(dto, FurnitureType.class);
        boolean success = this.save(type);
        return success ? Result.ok("添加成功") : Result.fail("添加失败");
    }

    @Override
    @Transactional
    public Result editFurnitureType(AdminFurnitureTypeFormDTO dto) {
        if (dto.getId() == null) {
            return Result.fail("ID 不能为空");
        }
        FurnitureType type = BeanUtil.toBean(dto, FurnitureType.class);
        boolean success = this.updateById(type);
        return success ? Result.ok("更新成功") : Result.fail("更新失败");
    }

    @Override
    @Transactional
    public Result deleteFurnitureType(Long id) {
        boolean success = this.removeById(id);
        return success ? Result.ok("删除成功") : Result.fail("删除失败");
    }

    @Override
    public Result getFurnitureTypeById(Long id) {
        FurnitureType type = this.getById(id);
        if (type == null) {
            return Result.fail("数据不存在");
        }
        AdminFurnitureTypeFormDTO dto = BeanUtil.toBean(type, AdminFurnitureTypeFormDTO.class);
        return Result.ok(dto);
    }

    @Override
    public Result getFurnitureTypeList(Integer current, Integer size, String typeName) {
        Page<FurnitureType> page = new Page<>(current, size);
        LambdaQueryWrapper<FurnitureType> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(typeName)) {
            queryWrapper.like(FurnitureType::getName, typeName);
        }
        Page<FurnitureType> resultPage = page(page, queryWrapper);
        if (resultPage.getTotal() == 0) {
            return Result.fail("暂未发现任何数据！");
        }
        return Result.ok(resultPage);
    }
}
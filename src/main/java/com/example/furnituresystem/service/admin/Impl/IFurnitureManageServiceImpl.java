package com.example.furnituresystem.service.admin.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.dto.admin.AdminFurnitureFormDTO;
import com.example.furnituresystem.entity.pojo.Furniture;
import com.example.furnituresystem.exception.BusinessException;
import com.example.furnituresystem.mapper.admin.FurnitureManageMapper;
import com.example.furnituresystem.service.admin.IFurnitureManageService;
import com.example.furnituresystem.utils.RedisConstants;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class IFurnitureManageServiceImpl extends ServiceImpl<FurnitureManageMapper, Furniture>
        implements IFurnitureManageService {

    @Resource
    private FurnitureManageMapper furnitureManageMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result getFurnitureList(Integer current, Integer size, Long typeId, String fName,
                                   String stockStatus, String brand) {
        Page<Furniture> page = new Page<>(current, size);
        LambdaQueryWrapper<Furniture> wrapper = new LambdaQueryWrapper<>();
        if (typeId != null) {
            wrapper.eq(Furniture::getTypeId, typeId);
        }
        if (StrUtil.isNotBlank(fName)) {
            wrapper.like(Furniture::getFName, fName);
        }
        if (StrUtil.isNotBlank(brand)) {
            wrapper.eq(Furniture::getBrand, brand);
        }
        if ("in_stock".equals(stockStatus)) {
            wrapper.gt(Furniture::getStock, 0);
        } else if ("low_stock".equals(stockStatus)) {
            wrapper.gt(Furniture::getStock, 0);
            wrapper.lt(Furniture::getStock, 10);
        } else if ("out_stock".equals(stockStatus)) {
            wrapper.eq(Furniture::getStock, 0);
        }
        Page<Furniture> result = furnitureManageMapper.selectPage(page, wrapper);
        return Result.ok(result);
    }

    @Override
    @Transactional
    public Result addFurniture(AdminFurnitureFormDTO dto) {
        if (dto == null) {
            return Result.fail("请输入完整的新增家具信息！");
        }
        Furniture furniture = BeanUtil.copyProperties(dto, Furniture.class);
        boolean success = save(furniture);
        if (!success) {
            throw new BusinessException("添加家具失败，请联系系统管理人员！");
        }
        return Result.ok();
    }

    @Override
    @Transactional
    public Result editFurniture(AdminFurnitureFormDTO dto) {
        if (dto == null || dto.getId() == null) {
            return Result.fail("请求参数错误");
        }
        Furniture furniture = furnitureManageMapper.selectById(dto.getId());
        if (furniture == null) {
            return Result.fail("家具不存在，无法修改");
        }
        furniture.setFName(dto.getFName());
        furniture.setFIcon(dto.getFIcon());
        furniture.setTypeId(dto.getTypeId());
        furniture.setPrice(dto.getPrice());
        furniture.setBrand(dto.getBrand());
        furniture.setStock(dto.getStock());
        if (StrUtil.isNotBlank(dto.getIntro())) {
            furniture.setIntro(dto.getIntro());
        }
        if (dto.getImages() != null) {
            furniture.setImages(dto.getImages());
        }
        boolean success = furnitureManageMapper.updateById(furniture) > 0;
        if (success) {
            stringRedisTemplate.delete(RedisConstants.CACHE_FURNITURE_KEY + dto.getId());
            return Result.ok("修改成功");
        } else {
            throw new BusinessException("修改失败，请系统联系管理人员！");
        }
    }

    @Override
    @Transactional
    public Result deleteFurniture(Long furnitureId) {
        int rows = furnitureManageMapper.deleteById(furnitureId);
        if (rows > 0) {
            stringRedisTemplate.delete(RedisConstants.CACHE_FURNITURE_KEY + furnitureId);
            return Result.ok();
        }
        return Result.fail("删除失败！");
    }

}

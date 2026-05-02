package com.example.furnituresystem.service.Impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.pojo.FurnitureType;
import com.example.furnituresystem.mapper.FurnitureTypeMapper;
import com.example.furnituresystem.service.IFurnitureTypeService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.furnituresystem.utils.RedisConstants.CACHE_FURNITURE_TYPE_KEY;
import static com.example.furnituresystem.utils.RedisConstants.CACHE_NULL_TTL;

@Service
public class IFurnitureTypeServiceImpl extends ServiceImpl<FurnitureTypeMapper, FurnitureType> implements IFurnitureTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryFurnitureTypeList() {
        String key = CACHE_FURNITURE_TYPE_KEY;
        String cacheTypeList = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(cacheTypeList)) {
            List<FurnitureType> typeList = JSONUtil.toList(cacheTypeList, FurnitureType.class);
            return Result.ok(typeList);
        }
        if (cacheTypeList != null) {
            return Result.ok(Collections.emptyList());
        }
        LambdaQueryWrapper<FurnitureType> wrapper = new LambdaQueryWrapper<>();
        List<FurnitureType> typeList = list(wrapper);
        if (typeList == null || typeList.isEmpty()) {
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return Result.ok(Collections.emptyList());
        }
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(typeList));
        return Result.ok(typeList);
    }

}

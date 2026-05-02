package com.example.furnituresystem.service.Impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.pojo.Furniture;
import com.example.furnituresystem.mapper.FurnitureMapper;
import com.example.furnituresystem.service.IFurnitureService;
import com.example.furnituresystem.utils.RedisData;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.example.furnituresystem.utils.RedisConstants.*;

@Service
@Slf4j
public class IFurnitureServiceImpl extends ServiceImpl<FurnitureMapper, Furniture> implements IFurnitureService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private FurnitureMapper furnitureMapper;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    @Override
    public Result queryFurnitureById(Long id) {
        String key = CACHE_FURNITURE_KEY + id;
        String furnitureJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(furnitureJson)) {
            RedisData redisData = JSONUtil.toBean(furnitureJson, RedisData.class);
            Furniture furniture = JSONUtil.toBean((JSONObject) redisData.getData(), Furniture.class);
            if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
                return Result.ok(furniture);
            }
            String lockKey = LOCK_FURNITURE_KEY + id;
            RLock lock = redissonClient.getLock(lockKey);
            boolean tryLock = false;
            try {
                tryLock = lock.tryLock(3, 10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("获取锁被中断, id={}", id);
            }
            if (tryLock) {
                CACHE_REBUILD_EXECUTOR.submit(() -> {
                    try {
                        log.info("开始重建缓存, id={}", id);
                        saveFurniture2Redis(id, 3600);
                    } catch (Exception e) {
                        log.error("重建缓存失败, id={}", id, e);
                    } finally {
                        lock.unlock();
                    }
                });
            }
            return Result.ok(furniture);
        }
        if (furnitureJson != null) {
            return Result.fail("该家具不存在，请刷新页面后重新选择！");
        }
        Furniture furniture = getById(id);
        return Result.ok(furniture);
    }

    @Override
    public Result getFurnitureByType(Long typeId, Integer current, Integer size,
                                     String fName, String stockStatus, String brand) {
        Page<Furniture> page = new Page<>(current, size);
        LambdaQueryWrapper<Furniture> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Furniture::getTypeId, typeId);
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
        return Result.ok(furnitureMapper.selectPage(page, wrapper));
    }

    @Override
    public Result getFurnitureBrandsByTypeId(Long id) {
        List<String> brands = furnitureMapper.getFurnitureBrandsByTypeId(id);
        if (brands == null || brands.isEmpty()) {
            return Result.fail("暂未查询到家具品牌！");
        }
        return Result.ok(brands);
    }

    private void saveFurniture2Redis(Long id, long expireSeconds) {
        Furniture furniture = getById(id);
        if (furniture == null) {
            stringRedisTemplate.opsForValue().set(CACHE_FURNITURE_KEY + id, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return;
        }
        RedisData redisData = new RedisData();
        redisData.setData(furniture);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        stringRedisTemplate.opsForValue().set(CACHE_FURNITURE_KEY + id, JSONUtil.toJsonStr(redisData));
    }

    public void setLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }


}

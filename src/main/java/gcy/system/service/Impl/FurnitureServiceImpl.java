package gcy.system.service.Impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.Furniture;
import gcy.system.entity.vo.TopFurnitureVO;
import gcy.system.mapper.FurnitureMapper;
import gcy.system.mapper.OrderItemMapper;
import gcy.system.service.IFurnitureService;
import gcy.system.utils.RedisData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static gcy.system.utils.RedisConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FurnitureServiceImpl extends ServiceImpl<FurnitureMapper, Furniture> implements IFurnitureService {

    private final StringRedisTemplate stringRedisTemplate;

    private final FurnitureMapper furnitureMapper;

    private final OrderItemMapper orderItemMapper;

    private final RedissonClient redissonClient;

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
                tryLock = lock.tryLock(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("获取锁被中断, id={}", id);
            }
            if (tryLock) {
                rebuildCacheAsync(id, lock);
            }
            return Result.ok(furniture);
        }
        if (furnitureJson != null) {
            return Result.fail(404, "该家具不存在，请刷新页面后重新选择！");
        }
        // 缓存完全缺失，加锁防止大量并发请求同时穿透到DB
        String lockKey = LOCK_FURNITURE_KEY + id;
        RLock lock = redissonClient.getLock(lockKey);
        boolean tryLock = false;
        try {
            tryLock = lock.tryLock(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取冷缓存锁被中断, id={}", id);
        }
        if (tryLock) {
            try {
                // Double-check: 可能其他线程已重建缓存
                String doubleCheck = stringRedisTemplate.opsForValue().get(key);
                if (StrUtil.isNotBlank(doubleCheck)) {
                    RedisData redisData = JSONUtil.toBean(doubleCheck, RedisData.class);
                    return Result.ok(JSONUtil.toBean((JSONObject) redisData.getData(), Furniture.class));
                }
                if (doubleCheck != null) {
                    return Result.fail(404, "该家具不存在，请刷新页面后重新选择！");
                }
                Furniture furniture = getById(id);
                if (furniture == null) {
                    stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                    return Result.fail(404, "该家具不存在，请刷新页面后重新选择！");
                }
                saveFurniture2Redis(id, CACHE_FURNITURE_TTL);
                return Result.ok(furniture);
            } finally {
                lock.unlock();
            }
        }
        // 获取锁失败，降级直接查DB
        Furniture furniture = getById(id);
        if (furniture == null) {
            return Result.fail(404, "该家具不存在，请刷新页面后重新选择！");
        }
        return Result.ok(furniture);
    }

    @Override
    public Result getFurnitureByType(Long typeId, Integer current, Integer size,
                                     String fName, String stockStatus, String brand,
                                     String sortBy, String sortOrder,
                                     Integer isRecommended) {
        Page<Furniture> page = new Page<>(current, size);
        LambdaQueryWrapper<Furniture> wrapper = new LambdaQueryWrapper<>();
        if (typeId != null && typeId > 0) {
            wrapper.eq(Furniture::getTypeId, typeId);
        }
        if (StrUtil.isNotBlank(fName)) {
            wrapper.like(Furniture::getFName, fName);
        }
        if (StrUtil.isNotBlank(brand)) {
            wrapper.eq(Furniture::getBrand, brand);
        }
        if (isRecommended != null && isRecommended == 1) {
            wrapper.eq(Furniture::getIsRecommended, 1);
        }
        applyStockStatusFilter(wrapper, stockStatus);
        applySorting(wrapper, sortBy, sortOrder);
        return Result.ok(furnitureMapper.selectPage(page, wrapper));
    }

    @Override
    public Result getTopSelling(Integer limit) {
        List<TopFurnitureVO> list = orderItemMapper.selectTopSelling(limit);
        return Result.ok(list);
    }

    private void applySorting(LambdaQueryWrapper<Furniture> wrapper, String sortBy, String sortOrder) {
        if (StrUtil.isBlank(sortBy) || "default".equals(sortBy)) {
            return;
        }
        boolean asc = "asc".equals(sortOrder);
        switch (sortBy) {
            case "price":
                if (asc) {
                    wrapper.orderByAsc(Furniture::getPrice);
                } else {
                    wrapper.orderByDesc(Furniture::getPrice);
                }
                break;
            case "sales":
                wrapper.gt(Furniture::getSaleCount, 0);
                wrapper.orderByDesc(Furniture::getSaleCount);
                break;
            case "newest":
                wrapper.ge(Furniture::getCreateTime, LocalDateTime.now().minusDays(3));
                wrapper.orderByDesc(Furniture::getCreateTime);
                break;
            default:
                break;
        }
    }

    public static void applyStockStatusFilter(LambdaQueryWrapper<Furniture> wrapper, String stockStatus) {
        if ("in_stock".equals(stockStatus)) {
            wrapper.gt(Furniture::getStock, 0);
        } else if ("low_stock".equals(stockStatus)) {
            wrapper.gt(Furniture::getStock, 0);
            wrapper.lt(Furniture::getStock, 10);
        } else if ("out_stock".equals(stockStatus)) {
            wrapper.eq(Furniture::getStock, 0);
        }
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

    public void rebuildCacheAsync(Long id, RLock lock) {
        try {
            log.info("开始重建缓存, id={}", id);
            saveFurniture2Redis(id, 3600);
        } catch (Exception e) {
            log.error("重建缓存失败, id={}", id, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}

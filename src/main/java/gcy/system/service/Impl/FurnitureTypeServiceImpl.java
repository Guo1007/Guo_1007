package gcy.system.service.Impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.FurnitureType;
import gcy.system.mapper.FurnitureTypeMapper;
import gcy.system.service.IFurnitureTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static gcy.system.utils.RedisConstants.*;

/**
 * 家具类型服务实现类。
 * <p>
 * 提供家具类型列表的查询功能，使用 Redis 缓存和 Redisson 分布式锁来保证缓存一致性，
 * 缓存未命中时查询数据库并回写缓存。获取锁失败时降级为直接查询数据库。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FurnitureTypeServiceImpl extends ServiceImpl<FurnitureTypeMapper, FurnitureType> implements IFurnitureTypeService {

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    /**
     * 查询家具类型列表。
     * <p>
     * 采用"缓存优先"策略：先查 Redis 缓存，命中则直接返回；
     * 若缓存值为空字符串（表示数据库中无数据），则返回空列表，避免缓存穿透；
     * 若缓存未命中，使用 Redisson 分布式锁控制并发，由获取锁的线程查询数据库并回写缓存，
     * 未获取到锁的线程降级为直接查询数据库。
     * </p>
     *
     * @return 包含家具类型列表的通用结果对象，列表为空时返回空集合
     */
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
        RLock lock = redissonClient.getLock(LOCK_FURNITURE_TYPE_KEY);
        boolean locked = false;
        try {
            locked = lock.tryLock(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取家具类型锁被中断");
        }
        if (locked) {
            try {
                String doubleCheck = stringRedisTemplate.opsForValue().get(key);
                if (StrUtil.isNotBlank(doubleCheck)) {
                    return Result.ok(JSONUtil.toList(doubleCheck, FurnitureType.class));
                }
                LambdaQueryWrapper<FurnitureType> wrapper = new LambdaQueryWrapper<>();
                List<FurnitureType> typeList = list(wrapper);
                if (typeList == null || typeList.isEmpty()) {
                    stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                    return Result.ok(Collections.emptyList());
                }
                stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(typeList), CACHE_FURNITURE_TTL, TimeUnit.MINUTES);
                return Result.ok(typeList);
            } finally {
                lock.unlock();
            }
        }
        // 获取锁失败，降级直接查DB
        LambdaQueryWrapper<FurnitureType> fallbackWrapper = new LambdaQueryWrapper<>();
        List<FurnitureType> fallbackList = list(fallbackWrapper);
        if (fallbackList == null || fallbackList.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        return Result.ok(fallbackList);
    }

}

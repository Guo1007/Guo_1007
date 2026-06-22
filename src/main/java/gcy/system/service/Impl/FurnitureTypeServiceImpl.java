package gcy.system.service.Impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.FurnitureType;
import gcy.system.mapper.FurnitureTypeMapper;
import gcy.system.service.IFurnitureTypeService;
import gcy.system.utils.JvmLockManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static gcy.system.utils.RedisConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FurnitureTypeServiceImpl extends ServiceImpl<FurnitureTypeMapper, FurnitureType> implements IFurnitureTypeService {

    private final StringRedisTemplate stringRedisTemplate;

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
        ReentrantLock lock = JvmLockManager.getLock(LOCK_FURNITURE_TYPE_KEY);
        boolean tryLock = false;
        try {
            tryLock = lock.tryLock(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取家具类型锁被中断");
        }
        if (tryLock) {
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
                stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(typeList));
                return Result.ok(typeList);
            } finally {
                lock.unlock();
            }
        }
        return Result.ok(Collections.emptyList());
    }

}

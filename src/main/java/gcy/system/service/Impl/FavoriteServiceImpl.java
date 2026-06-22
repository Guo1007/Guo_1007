package gcy.system.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.Favorite;
import gcy.system.exception.BusinessException;
import gcy.system.mapper.FavoriteMapper;
import gcy.system.service.IFavoriteService;
import gcy.system.utils.JvmLockManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements IFavoriteService {

    @Resource
    private FavoriteMapper favoriteMapper;

    @Override
    public Result getFavoritesByUserId(Long userId, Integer current, Integer size) {
        Page<Map<String, Object>> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        Page<Map<String, Object>> result = favoriteMapper.selectFavoritesWithFurniturePage(userId, page);
        return Result.ok(result);
    }

    @Override
    public Result checkFavorite(Long userId, Long furnitureId) {
        boolean exists = favoriteMapper.existsByUserIdAndFurnitureId(userId, furnitureId);
        return Result.ok(exists);
    }

    @Override
    public Result toggleFavorite(Long userId, Long furnitureId) {
        String lockKey = "lock:favorite:" + userId + ":" + furnitureId;
        ReentrantLock lock = JvmLockManager.getLock(lockKey);
        try {
            boolean locked = lock.tryLock(3, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("操作处理中，请稍后再试");
            }
            try {
                LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Favorite::getUserId, userId)
                        .eq(Favorite::getFurnitureId, furnitureId);
                Favorite existing = favoriteMapper.selectOne(wrapper);

                if (existing != null) {
                    favoriteMapper.deleteById(existing.getId());
                    return Result.ok(false);
                } else {
                    Favorite fav = new Favorite();
                    fav.setUserId(userId);
                    fav.setFurnitureId(furnitureId);
                    favoriteMapper.insert(fav);
                    return Result.ok(true);
                }
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取锁被中断: userId={}, furnitureId={}", userId, furnitureId);
            throw new BusinessException("系统繁忙，请稍后再试");
        }
    }
}

package gcy.system.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.Favorite;
import gcy.system.entity.vo.FavoriteVO;
import gcy.system.exception.BusinessException;
import gcy.system.mapper.FavoriteMapper;
import gcy.system.service.IFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static gcy.system.utils.RedisConstants.LOCK_FAVORITE_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements IFavoriteService {

    private final FavoriteMapper favoriteMapper;

    private final RedissonClient redissonClient;

    @Override
    public Result getFavoritesByUserId(Long userId, Integer current, Integer size) {
        Page<FavoriteVO> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        Page<FavoriteVO> result = favoriteMapper.selectFavoritesWithFurniturePage(userId, page);
        return Result.ok(result);
    }

    @Override
    public Result checkFavorite(Long userId, Long furnitureId) {
        boolean exists = favoriteMapper.existsByUserIdAndFurnitureId(userId, furnitureId);
        return Result.ok(exists);
    }

    @Override
    public Result toggleFavorite(Long userId, Long furnitureId) {
        String lockKey = LOCK_FAVORITE_KEY + userId + ":" + furnitureId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(3, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("操作处理中，请稍后再试");
            }
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取锁被中断: userId={}, furnitureId={}", userId, furnitureId);
            throw new BusinessException("系统繁忙，请稍后再试");
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }
}

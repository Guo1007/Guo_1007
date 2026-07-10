package gcy.system.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.Favorite;
import gcy.system.entity.vo.FavoriteVO;
import gcy.system.exception.BusinessException;
import gcy.system.mapper.FavoriteMapper;
import gcy.system.mapper.FurnitureMapper;
import gcy.system.service.IFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements IFavoriteService {

    private final FavoriteMapper favoriteMapper;

    private final FurnitureMapper furnitureMapper;

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
    @Transactional
    public Result toggleFavorite(Long userId, Long furnitureId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getFurnitureId, furnitureId);
        Favorite existing = favoriteMapper.selectOne(wrapper);

        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return Result.ok(false);
        }
        // 校验家具是否存在
        if (furnitureMapper.selectById(furnitureId) == null) {
            throw new BusinessException("商品不存在或已下架");
        }
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setFurnitureId(furnitureId);
        try {
            favoriteMapper.insert(fav);
            return Result.ok(true);
        } catch (DuplicateKeyException e) {
            // 并发点击：数据库唯一索引已阻止重复插入，直接当作已收藏
            log.debug("重复收藏被唯一索引拦截: userId={}, furnitureId={}", userId, furnitureId);
            return Result.ok(true);
        }
    }
}

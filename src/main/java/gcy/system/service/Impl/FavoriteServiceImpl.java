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

/**
 * 用户收藏服务实现类
 * <p>
 * 提供收藏列表分页查询、收藏状态检查、收藏/取消收藏切换等核心业务逻辑。
 * 基于 MyBatis-Plus 的 ServiceImpl 实现，依赖 FavoriteMapper 和 FurnitureMapper 完成数据操作。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements IFavoriteService {

    private final FavoriteMapper favoriteMapper;

    private final FurnitureMapper furnitureMapper;

    /**
     * 根据用户ID分页查询收藏列表
     * <p>
     * 通过 FavoriteMapper 的自定义 SQL 关联查询家具详情，
     * 将收藏记录与家具信息一并封装到 FavoriteVO 中返回。
     * 当分页参数为 null 时，默认使用第1页、每页10条。
     * </p>
     *
     * @param userId 用户ID，用于筛选该用户的收藏记录
     * @param current 当前页码，为 null 时默认为 1
     * @param size 每页条数，为 null 时默认为 10
     * @return 包含分页 FavoriteVO 数据的统一响应结果
     */
    @Override
    public Result getFavoritesByUserId(Long userId, Integer current, Integer size) {
        Page<FavoriteVO> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        Page<FavoriteVO> result = favoriteMapper.selectFavoritesWithFurniturePage(userId, page);
        return Result.ok(result);
    }

    /**
     * 检查用户是否已收藏指定家具
     * <p>
     * 调用 FavoriteMapper 查询用户与家具的收藏关联记录是否存在，
     * 返回布尔值表示是否已收藏。
     * </p>
     *
     * @param userId 用户ID，标识当前操作的收藏者
     * @param furnitureId 家具ID，标识待检查的目标家具
     * @return 包含布尔值（true 已收藏，false 未收藏）的统一响应结果
     */
    @Override
    public Result checkFavorite(Long userId, Long furnitureId) {
        boolean exists = favoriteMapper.existsByUserIdAndFurnitureId(userId, furnitureId);
        return Result.ok(exists);
    }

    /**
     * 切换收藏状态（收藏/取消收藏）
     * <p>
     * 如果用户已收藏该家具，则删除收藏记录并返回 false；
     * 如果未收藏，则先校验家具是否存在，存在则插入收藏记录并返回 true。
     * 通过数据库唯一索引防止并发重复插入，捕获 DuplicateKeyException 后直接视为已收藏。
     * </p>
     *
     * @param userId 用户ID，标识当前操作的用户
     * @param furnitureId 家具ID，标识待切换收藏状态的目标家具
     * @return 包含布尔值（true 表示已收藏，false 表示已取消收藏）的统一响应结果
     * @throws BusinessException 当目标家具不存在或已下架时抛出业务异常
     */
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

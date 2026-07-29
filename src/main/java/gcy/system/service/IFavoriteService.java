package gcy.system.service;

import gcy.system.entity.dto.Result;

/**
 * 用户收藏服务接口，提供收藏列表查询、收藏状态检查及收藏/取消收藏功能。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface IFavoriteService {

    /**
     * 分页查询指定用户的收藏列表。
     *
     * @param userId  用户ID，用于定位该用户的收藏数据
     * @param current 当前页码，用于分页查询
     * @param size    每页记录数，用于分页查询
     * @return 包含分页收藏数据的结果对象
     */
    Result getFavoritesByUserId(Long userId, Integer current, Integer size);

    /**
     * 检查指定用户是否已收藏某个家具。
     *
     * @param userId      用户ID，用于定位该用户的收藏数据
     * @param furnitureId 家具ID，用于标识待检查的家具
     * @return 包含收藏状态（已收藏/未收藏）的结果对象
     */
    Result checkFavorite(Long userId, Long furnitureId);

    /**
     * 切换指定用户对某个家具的收藏状态：若未收藏则添加收藏，若已收藏则取消收藏。
     *
     * @param userId      用户ID，用于定位该用户的收藏数据
     * @param furnitureId 家具ID，用于标识待切换收藏状态的家具
     * @return 包含切换后收藏状态的结果对象
     */
    Result toggleFavorite(Long userId, Long furnitureId);
}

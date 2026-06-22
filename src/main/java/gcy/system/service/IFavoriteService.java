package gcy.system.service;

import gcy.system.entity.dto.Result;

public interface IFavoriteService {

    Result getFavoritesByUserId(Long userId, Integer current, Integer size);

    Result checkFavorite(Long userId, Long furnitureId);

    Result toggleFavorite(Long userId, Long furnitureId);
}

package com.example.furnituresystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.pojo.Favorite;
import com.example.furnituresystem.mapper.FavoriteMapper;
import com.example.furnituresystem.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Resource
    private FavoriteMapper favoriteMapper;

    @GetMapping("/list")
    public Result list() {
        Long userId = UserHolder.getUser().getId();
        List<Map<String, Object>> list = favoriteMapper.selectFavoritesWithFurniture(userId);
        return Result.ok(list);
    }

    @GetMapping("/check/{furnitureId}")
    public Result check(@PathVariable Long furnitureId) {
        Long userId = UserHolder.getUser().getId();
        boolean exists = favoriteMapper.existsByUserIdAndFurnitureId(userId, furnitureId);
        return Result.ok(exists);
    }

    @PostMapping("/toggle/{furnitureId}")
    public Result toggle(@PathVariable Long furnitureId) {
        Long userId = UserHolder.getUser().getId();
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getFurnitureId, furnitureId);
        Favorite existing = favoriteMapper.selectOne(wrapper);

        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return Result.ok(false); // un-favorited
        } else {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setFurnitureId(furnitureId);
            favoriteMapper.insert(fav);
            return Result.ok(true); // favorited
        }
    }
}

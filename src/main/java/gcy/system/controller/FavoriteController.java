package gcy.system.controller;

import gcy.system.entity.dto.Result;
import gcy.system.service.IFavoriteService;
import gcy.system.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final IFavoriteService favoriteService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserHolder.getUser().getId();
        return favoriteService.getFavoritesByUserId(userId, current, size);
    }

    @GetMapping("/check/{furnitureId}")
    public Result check(@PathVariable Long furnitureId) {
        Long userId = UserHolder.getUser().getId();
        return favoriteService.checkFavorite(userId, furnitureId);
    }

    @PostMapping("/toggle/{furnitureId}")
    public Result toggle(@PathVariable Long furnitureId) {
        Long userId = UserHolder.getUser().getId();
        return favoriteService.toggleFavorite(userId, furnitureId);
    }
}

package gcy.system.controller;

import gcy.system.entity.dto.Result;
import gcy.system.service.IFavoriteService;
import gcy.system.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 * <p>
 * 处理用户收藏相关的HTTP请求，包括收藏列表查询、收藏状态检查和收藏切换操作。
 * 基础请求路径为 /favorite。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Tag(name = "收藏管理", description = "收藏管理相关接口")
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final IFavoriteService favoriteService;

    /**
     * 获取当前用户的收藏列表
     * <p>
     * GET /favorite/list?current=1&amp;size=10
     * 从当前登录用户上下文中获取用户ID，查询该用户的收藏记录并分页返回。
     * </p>
     *
     * @param current 当前页码，默认值为1
     * @param size    每页记录数，默认值为10
     * @return 包含分页收藏记录的结果对象
     */
    @Operation(summary = "获取当前用户的收藏列表")
    @GetMapping("/list")
    public Result list(@Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
                       @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserHolder.getUser().getId();
        return favoriteService.getFavoritesByUserId(userId, current, size);
    }

    /**
     * 检查指定家具是否已被当前用户收藏
     * <p>
     * GET /favorite/check/{furnitureId}
     * 根据家具ID和当前登录用户判断该家具是否已存在于用户的收藏列表中。
     * </p>
     *
     * @param furnitureId 家具的唯一标识ID
     * @return 包含收藏状态（已收藏/未收藏）的结果对象
     */
    @Operation(summary = "检查指定家具是否已被收藏")
    @GetMapping("/check/{furnitureId}")
    public Result check(@Parameter(description = "家具ID") @PathVariable Long furnitureId) {
        Long userId = UserHolder.getUser().getId();
        return favoriteService.checkFavorite(userId, furnitureId);
    }

    /**
     * 切换指定家具的收藏状态
     * <p>
     * POST /favorite/toggle/{furnitureId}
     * 如果该家具未被当前用户收藏则添加收藏，如果已收藏则取消收藏。
     * </p>
     *
     * @param furnitureId 家具的唯一标识ID
     * @return 包含切换后收藏状态的结果对象
     */
    @Operation(summary = "切换指定家具的收藏状态")
    @PostMapping("/toggle/{furnitureId}")
    public Result toggle(@Parameter(description = "家具ID") @PathVariable Long furnitureId) {
        Long userId = UserHolder.getUser().getId();
        return favoriteService.toggleFavorite(userId, furnitureId);
    }

}

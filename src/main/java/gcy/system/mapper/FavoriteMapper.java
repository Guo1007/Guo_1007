package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import gcy.system.entity.pojo.Favorite;
import gcy.system.entity.vo.FavoriteVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 收藏夹数据访问层接口。
 * <p>
 * 提供用户收藏家具相关的数据库操作，包括分页查询收藏列表和判断收藏是否存在。
 * 继承 MyBatis-Plus 的 BaseMapper，自动拥有基础 CRUD 能力。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 分页查询指定用户的收藏家具列表。
     * <p>
     * 通过 INNER JOIN 关联家具表（furniture），根据用户 ID 筛选其收藏记录，
     * 排除已软删除的家具（deleted = 0），并按收藏创建时间倒序排列。
     * </p>
     *
     * @param userId 用户 ID，用于筛选该用户的收藏记录
     * @param page   分页对象，包含页码和每页条数信息
     * @return 分页后的收藏家具视图对象列表（FavoriteVO），包含家具名称、图标、价格、库存、简介、品牌等信息
     */
    @Select("SELECT f.id, f.f_name, f.f_icon, f.price, f.stock, f.intro, f.brand " +
            "FROM favorite fav INNER JOIN furniture f ON fav.furniture_id = f.id " +
            "WHERE fav.user_id = #{userId} AND f.deleted = 0 ORDER BY fav.create_time DESC")
    Page<FavoriteVO> selectFavoritesWithFurniturePage(@Param("userId") Long userId, Page<FavoriteVO> page);

    /**
     * 判断指定用户是否已收藏某件家具。
     * <p>
     * 查询收藏表中是否存在 user_id 和 furniture_id 同时匹配的记录，
     * 若存在则返回 true，否则返回 false。
     * </p>
     *
     * @param userId      用户 ID
     * @param furnitureId 家具 ID
     * @return 如果该用户已收藏该家具则返回 true，否则返回 false
     */
    @Select("SELECT COUNT(*) > 0 FROM favorite fav INNER JOIN furniture f ON fav.furniture_id = f.id " +
            "WHERE fav.user_id = #{userId} " +
            "AND fav.furniture_id = #{furnitureId} " +
            "AND f.deleted = 0")
    boolean existsByUserIdAndFurnitureId(@Param("userId") Long userId, @Param("furnitureId") Long furnitureId);
}

package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import gcy.system.entity.pojo.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT f.* FROM favorite fav INNER JOIN furniture f ON fav.furniture_id = f.id " +
            "WHERE fav.user_id = #{userId} ORDER BY fav.create_time DESC")
    Page<Map<String, Object>> selectFavoritesWithFurniturePage(@Param("userId") Long userId, Page<Map<String, Object>> page);

    @Select("SELECT COUNT(*) > 0 FROM favorite " +
            "WHERE user_id = #{userId} " +
            "AND furniture_id = #{furnitureId}")
    boolean existsByUserIdAndFurnitureId(@Param("userId") Long userId, @Param("furnitureId") Long furnitureId);
}

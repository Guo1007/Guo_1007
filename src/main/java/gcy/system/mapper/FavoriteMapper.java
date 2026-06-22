package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import gcy.system.entity.pojo.Favorite;
import gcy.system.entity.vo.FavoriteVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT f.id, f.f_name, f.f_icon, f.price, f.stock, f.intro, f.brand " +
            "FROM favorite fav INNER JOIN furniture f ON fav.furniture_id = f.id " +
            "WHERE fav.user_id = #{userId} ORDER BY fav.create_time DESC")
    Page<FavoriteVO> selectFavoritesWithFurniturePage(@Param("userId") Long userId, Page<FavoriteVO> page);

    @Select("SELECT COUNT(*) > 0 FROM favorite " +
            "WHERE user_id = #{userId} " +
            "AND furniture_id = #{furnitureId}")
    boolean existsByUserIdAndFurnitureId(@Param("userId") Long userId, @Param("furnitureId") Long furnitureId);
}


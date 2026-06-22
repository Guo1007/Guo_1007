package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT r.*, u.user_name FROM review r LEFT JOIN user u ON r.user_id = u.id " +
            "WHERE r.furniture_id = #{furnitureId} ORDER BY r.create_time DESC")
    List<Map<String, Object>> selectReviewsByFurnitureId(@Param("furnitureId") Long furnitureId);

    @Select("SELECT COALESCE(AVG(rating), 0) AS avg_rating, COUNT(*) AS review_count " +
            "FROM review WHERE furniture_id = #{furnitureId}")
    Map<String, Object> selectRatingStats(@Param("furnitureId") Long furnitureId);
}

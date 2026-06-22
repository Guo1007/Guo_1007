package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.Review;
import gcy.system.entity.vo.ReviewStatsVO;
import gcy.system.entity.vo.ReviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT r.id, r.user_id, u.user_name, u.icon AS user_avatar, r.order_id, r.furniture_id, r.rating, r.content, r.create_time " +
            "FROM review r LEFT JOIN user u ON r.user_id = u.id " +
            "WHERE r.furniture_id = #{furnitureId} ORDER BY r.create_time DESC")
    List<ReviewVO> selectReviewsByFurnitureId(@Param("furnitureId") Long furnitureId);

    @Select("SELECT COALESCE(AVG(rating), 0) AS avg_rating, COUNT(*) AS review_count " +
            "FROM review WHERE furniture_id = #{furnitureId}")
    ReviewStatsVO selectRatingStats(@Param("furnitureId") Long furnitureId);
}

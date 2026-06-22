package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.ReviewComment;
import gcy.system.entity.vo.ReviewCommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewCommentMapper extends BaseMapper<ReviewComment> {

    @Select("SELECT rc.*, u.user_name, u.icon AS user_avatar, " +
            "ru.user_name AS reply_to_user_name " +
            "FROM review_comment rc " +
            "LEFT JOIN user u ON rc.user_id = u.id " +
            "LEFT JOIN user ru ON rc.reply_to_user_id = ru.id " +
            "WHERE rc.review_id = #{reviewId} " +
            "AND (rc.status = 1 OR rc.user_id = #{userId}) " +
            "ORDER BY rc.create_time ASC")
    List<ReviewCommentVO> selectByReviewId(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
}

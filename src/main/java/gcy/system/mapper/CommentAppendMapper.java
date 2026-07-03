package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.CommentAppend;
import gcy.system.entity.vo.CommentAppendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentAppendMapper extends BaseMapper<CommentAppend> {

    @Select("SELECT ca.*, u.user_name, u.icon AS user_avatar " +
            "FROM comment_append ca " +
            "LEFT JOIN user u ON ca.user_id = u.id " +
            "WHERE ca.main_comment_id = #{mainCommentId} " +
            "AND (ca.status = 1 OR ca.user_id = #{userId}) " +
            "ORDER BY ca.append_time ASC")
    List<CommentAppendVO> selectByMainCommentId(@Param("mainCommentId") Long mainCommentId, @Param("userId") Long userId);

    @Select("<script>SELECT ca.*, u.user_name, u.icon AS user_avatar " +
            "FROM comment_append ca " +
            "LEFT JOIN user u ON ca.user_id = u.id " +
            "WHERE ca.main_comment_id IN " +
            "<foreach collection='mainCommentIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}</foreach> " +
            "AND (ca.status = 1 OR ca.user_id = #{userId}) " +
            "ORDER BY ca.append_time ASC</script>")
    List<CommentAppendVO> selectByMainCommentIds(@Param("mainCommentIds") List<Long> mainCommentIds, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM comment_append WHERE main_comment_id = #{mainCommentId}")
    int countByMainCommentId(@Param("mainCommentId") Long mainCommentId);
}

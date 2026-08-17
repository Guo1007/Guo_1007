package gcy.system.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import gcy.system.entity.pojo.GoodsComment;
import gcy.system.entity.vo.admin.AdminAppendVO;
import gcy.system.entity.vo.admin.AdminCommentVO;
import gcy.system.entity.vo.admin.AdminReviewCommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论管理 Mapper 接口
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Mapper
public interface CommentManageMapper extends BaseMapper<GoodsComment> {

    @Select("<script>" +
            "SELECT gc.id, gc.order_id, gc.goods_id, gc.user_id, gc.score, gc.content, " +
            "gc.img_url, gc.video_url, gc.is_anonym, gc.status, gc.ai_reject_reason, gc.manual_reject_reason, gc.create_time, " +
            "u.user_name AS user_name, f.f_name AS goods_name " +
            "FROM goods_comment gc " +
            "LEFT JOIN user u ON gc.user_id = u.id " +
            "LEFT JOIN furniture f ON gc.goods_id = f.id " +
            "WHERE gc.deleted = 0 " +
            "<if test='statusList != null and statusList.size() > 0'>" +
            "AND gc.status IN " +
            "<foreach collection='statusList' item='s' open='(' separator=',' close=')'>" +
            "#{s}" +
            "</foreach>" +
            "</if>" +
            "ORDER BY gc.create_time DESC" +
            "</script>")
    Page<AdminCommentVO> selectAllComments(Page<AdminCommentVO> page, @Param("statusList") List<Integer> statusList);

    @Select("<script>" +
            "SELECT ca.id, ca.main_comment_id, ca.user_id, ca.append_content, ca.append_img, " +
            "ca.append_num, ca.status, ca.ai_reject_reason, ca.manual_reject_reason, ca.append_time, " +
            "u.user_name AS user_name, f.f_name AS goods_name " +
            "FROM comment_append ca " +
            "LEFT JOIN user u ON ca.user_id = u.id " +
            "LEFT JOIN goods_comment gc ON ca.main_comment_id = gc.id " +
            "LEFT JOIN furniture f ON gc.goods_id = f.id " +
            "WHERE ca.deleted = 0 " +
            "<if test='statusList != null and statusList.size() > 0'>" +
            "AND ca.status IN " +
            "<foreach collection='statusList' item='s' open='(' separator=',' close=')'>" +
            "#{s}" +
            "</foreach>" +
            "</if>" +
            "ORDER BY ca.append_time DESC" +
            "</script>")
    Page<AdminAppendVO> selectAllAppends(Page<AdminAppendVO> page, @Param("statusList") List<Integer> statusList);

    @Select("<script>" +
            "SELECT rc.id, rc.review_id, rc.user_id, rc.content, rc.reply_to_user_id, " +
            "rc.reply_to_comment_id, rc.status, rc.ai_reject_reason, rc.manual_reject_reason, rc.create_time, " +
            "u.user_name AS user_name, ru.user_name AS reply_to_user_name " +
            "FROM review_comment rc " +
            "LEFT JOIN user u ON rc.user_id = u.id " +
            "LEFT JOIN user ru ON rc.reply_to_user_id = ru.id " +
            "WHERE rc.deleted = 0 " +
            "<if test='statusList != null and statusList.size() > 0'>" +
            "AND rc.status IN " +
            "<foreach collection='statusList' item='s' open='(' separator=',' close=')'>" +
            "#{s}" +
            "</foreach>" +
            "</if>" +
            "ORDER BY rc.create_time DESC" +
            "</script>")
    Page<AdminReviewCommentVO> selectAllReviewComments(Page<AdminReviewCommentVO> page, @Param("statusList") List<Integer> statusList);
}
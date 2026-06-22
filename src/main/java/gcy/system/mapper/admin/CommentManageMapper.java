package gcy.system.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import gcy.system.entity.pojo.GoodsComment;
import gcy.system.entity.vo.admin.AdminAppendVO;
import gcy.system.entity.vo.admin.AdminCommentVO;
import gcy.system.entity.vo.admin.AdminReviewCommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CommentManageMapper extends BaseMapper<GoodsComment> {

    @Select("SELECT gc.*, u.user_name AS user_name, f.f_name AS goods_name " +
            "FROM goods_comment gc " +
            "LEFT JOIN user u ON gc.user_id = u.id " +
            "LEFT JOIN furniture f ON gc.goods_id = f.id " +
            "ORDER BY gc.create_time DESC")
    Page<AdminCommentVO> selectAllComments(Page<AdminCommentVO> page);

    @Select("SELECT ca.*, u.user_name AS user_name, f.f_name AS goods_name " +
            "FROM comment_append ca " +
            "LEFT JOIN user u ON ca.user_id = u.id " +
            "LEFT JOIN goods_comment gc ON ca.main_comment_id = gc.id " +
            "LEFT JOIN furniture f ON gc.goods_id = f.id " +
            "ORDER BY ca.append_time DESC")
    Page<AdminAppendVO> selectAllAppends(Page<AdminAppendVO> page);

    @Select("SELECT rc.*, u.user_name AS user_name, ru.user_name AS reply_to_user_name " +
            "FROM review_comment rc " +
            "LEFT JOIN user u ON rc.user_id = u.id " +
            "LEFT JOIN user ru ON rc.reply_to_user_id = ru.id " +
            "ORDER BY rc.create_time DESC")
    Page<AdminReviewCommentVO> selectAllReviewComments(Page<AdminReviewCommentVO> page);
}

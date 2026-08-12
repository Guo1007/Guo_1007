package gcy.system.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import gcy.system.entity.pojo.GoodsComment;
import gcy.system.entity.vo.admin.AdminAppendVO;
import gcy.system.entity.vo.admin.AdminCommentVO;
import gcy.system.entity.vo.admin.AdminReviewCommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 评论管理 Mapper 接口
 * <p>
 * 提供评论管理相关数据的数据库查询操作，包括商品评论查询、
 * 评论追加查询以及回复评论查询。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Mapper
public interface CommentManageMapper extends BaseMapper<GoodsComment> {

    /**
     * 分页查询所有未删除的商品评论
     * <p>
     * 查询 goods_comment 表中所有 deleted = 0 的评论记录，
     * 同时关联 user 表获取评论用户名，关联 furniture 表获取商品名称，
     * 按创建时间降序排列。
     * </p>
     *
     * @param page MyBatis Plus 分页对象，包含当前页码和每页条数
     * @return 包含 AdminCommentVO 数据的 Page 对象，每个元素持有评论信息、用户名和商品名
     */
    @Select("SELECT gc.id, gc.order_id, gc.goods_id, gc.user_id, gc.score, gc.content, " +
            "gc.img_url, gc.video_url, gc.is_anonym, gc.status, gc.create_time, " +
            "u.user_name AS user_name, f.f_name AS goods_name " +
            "FROM goods_comment gc " +
            "LEFT JOIN user u ON gc.user_id = u.id " +
            "LEFT JOIN furniture f ON gc.goods_id = f.id " +
            "WHERE gc.deleted = 0 " +
            "ORDER BY gc.create_time DESC")
    Page<AdminCommentVO> selectAllComments(Page<AdminCommentVO> page);

    /**
     * 分页查询所有未删除的评论追加记录
     * <p>
     * 查询 comment_append 表中所有 deleted = 0 的追加评论记录，
     * 关联 user 表获取追加评论用户名，通过 goods_comment 关联 furniture 表获取商品名称，
     * 按追加时间降序排列。
     * </p>
     *
     * @param page MyBatis Plus 分页对象，包含当前页码和每页条数
     * @return 包含 AdminAppendVO 数据的 Page 对象，每个元素持有追加评论信息、用户名和商品名
     */
    @Select("SELECT ca.id, ca.main_comment_id, ca.user_id, ca.append_content, ca.append_img, " +
            "ca.append_num, ca.status, ca.append_time, " +
            "u.user_name AS user_name, f.f_name AS goods_name " +
            "FROM comment_append ca " +
            "LEFT JOIN user u ON ca.user_id = u.id " +
            "LEFT JOIN goods_comment gc ON ca.main_comment_id = gc.id " +
            "LEFT JOIN furniture f ON gc.goods_id = f.id " +
            "WHERE ca.deleted = 0 " +
            "ORDER BY ca.append_time DESC")
    Page<AdminAppendVO> selectAllAppends(Page<AdminAppendVO> page);

    /**
     * 分页查询所有未删除的回复评论
     * <p>
     * 查询 review_comment 表中所有 deleted = 0 的回复评论记录，
     * 关联 user 表分别获取评论人和被回复人的用户名，
     * 按创建时间降序排列。
     * </p>
     *
     * @param page MyBatis Plus 分页对象，包含当前页码和每页条数
     * @return 包含 AdminReviewCommentVO 数据的 Page 对象，每个元素持有回复评论信息、评论人用户名和被回复人用户名
     */
    @Select("SELECT rc.id, rc.review_id, rc.user_id, rc.content, rc.reply_to_user_id, " +
            "rc.reply_to_comment_id, rc.status, rc.create_time, " +
            "u.user_name AS user_name, ru.user_name AS reply_to_user_name " +
            "FROM review_comment rc " +
            "LEFT JOIN user u ON rc.user_id = u.id " +
            "LEFT JOIN user ru ON rc.reply_to_user_id = ru.id " +
            "WHERE rc.deleted = 0 " +
            "ORDER BY rc.create_time DESC")
    Page<AdminReviewCommentVO> selectAllReviewComments(Page<AdminReviewCommentVO> page);
}

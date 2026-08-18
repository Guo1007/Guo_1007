package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.ReviewComment;
import gcy.system.entity.vo.ReviewCommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论Mapper接口
 * <p>
 * 提供对review_comment表的数据访问操作，继承MyBatis-Plus的BaseMapper，
 * 具备基本的CRUD能力，并扩展自定义的评论查询方法。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Mapper
public interface ReviewCommentMapper extends BaseMapper<ReviewComment> {

    /**
     * 根据评测ID和用户ID查询评论列表
     * <p>
     * 关联user表获取评论者用户名和头像，以及被回复者的用户名。
     * 查询条件包括：评论未被逻辑删除（deleted=0），
     * 用户未主动删除或仅本人删除（user_deleted逻辑），
     * 评论已审核通过或为当前用户自己的评论（status逻辑）。
     * 结果按创建时间升序排列。
     * </p>
     *
     * @param reviewId 评测ID，用于筛选属于指定评测的评论
     * @param userId   当前登录用户ID，用于权限控制（可查看自己的评论及他人的已通过评论）
     * @return 评论视图对象列表，包含评论内容、评论者信息、被回复者信息等
     */
    @Select("SELECT rc.*, u.user_name, u.icon AS user_avatar, " +
            "ru.user_name AS reply_to_user_name " +
            "FROM review_comment rc " +
            "LEFT JOIN user u ON rc.user_id = u.id " +
            "LEFT JOIN user ru ON rc.reply_to_user_id = ru.id " +
            "WHERE rc.review_id = #{reviewId} AND rc.deleted = 0 " +
            "AND (rc.user_deleted = 0 OR (rc.user_deleted = 1 AND rc.user_id = #{userId})) " +
            "AND rc.status = 1 " +
            "ORDER BY rc.create_time ASC")
    List<ReviewCommentVO> selectByReviewId(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
}

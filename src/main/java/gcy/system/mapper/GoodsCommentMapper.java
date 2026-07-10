package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import gcy.system.entity.pojo.GoodsComment;
import gcy.system.entity.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GoodsCommentMapper extends BaseMapper<GoodsComment> {

    @Select("SELECT gc.*, u.user_name, u.icon AS user_avatar " +
            "FROM goods_comment gc " +
            "LEFT JOIN user u ON gc.user_id = u.id " +
            "WHERE gc.goods_id = #{goodsId} AND gc.deleted = 0 AND gc.user_deleted = 0 " +
            "AND (gc.status = 1 OR gc.user_id = #{userId}) " +
            "ORDER BY gc.create_time DESC")
    Page<CommentVO> selectCommentsByGoodsId(@Param("goodsId") Long goodsId, @Param("userId") Long userId, Page<CommentVO> page);

    @Select("SELECT gc.*, u.user_name, u.icon AS user_avatar " +
            "FROM goods_comment gc " +
            "LEFT JOIN user u ON gc.user_id = u.id " +
            "WHERE gc.goods_id = #{goodsId} AND gc.deleted = 0 AND gc.user_deleted = 0 " +
            "AND (gc.status = 1 OR gc.user_id = #{userId}) " +
            "ORDER BY gc.create_time DESC")
    List<CommentVO> selectAllCommentsByGoodsId(@Param("goodsId") Long goodsId, @Param("userId") Long userId);

    @Select("SELECT * FROM goods_comment WHERE order_id = #{orderId} AND user_id = #{userId} AND goods_id = #{goodsId} AND deleted = 0 AND user_deleted = 0")
    GoodsComment selectByOrderAndGoods(@Param("orderId") Long orderId, @Param("userId") Long userId, @Param("goodsId") Long goodsId);

    @Select("SELECT * FROM goods_comment WHERE user_id = #{userId} AND goods_id = #{goodsId} AND deleted = 0 AND user_deleted = 0")
    GoodsComment selectByUserAndGoods(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    @Select("SELECT gc.*, u.user_name, u.icon AS user_avatar " +
            "FROM goods_comment gc " +
            "LEFT JOIN user u ON gc.user_id = u.id " +
            "WHERE gc.order_id = #{orderId} " +
            "AND (gc.deleted = 0 OR (gc.deleted = 1 AND gc.user_id = #{userId})) " +
            "AND (gc.user_deleted = 0 OR (gc.user_deleted = 1 AND gc.user_id = #{userId})) " +
            "AND (gc.status = 1 OR gc.user_id = #{userId}) " +
            "ORDER BY gc.create_time DESC")
    List<CommentVO> selectCommentsByOrderId(@Param("orderId") Long orderId, @Param("userId") Long userId);
}

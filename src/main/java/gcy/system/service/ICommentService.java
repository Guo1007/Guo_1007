package gcy.system.service;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.CommentAppend;
import gcy.system.entity.pojo.GoodsComment;

/**
 * 评论服务接口
 * <p>
 * 提供商品评论的增删查、追评、回复等业务操作。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface ICommentService {

    /**
     * 根据商品ID分页查询评论列表
     * <p>
     * 查询指定商品下的所有评论，支持分页展示，并根据当前用户ID标记是否为本人评论。
     * </p>
     *
     * @param goodsId 商品ID，用于定位具体商品的评论
     * @param userId  当前登录用户ID，用于区分评论是否为本人所发
     * @param current 当前页码，从1开始
     * @param size    每页显示的评论条数
     * @return 包含分页评论数据和分页信息的统一结果对象
     */
    Result getCommentsByGoodsId(Long goodsId, Long userId, Integer current, Integer size);

    /**
     * 根据订单ID查询评论
     * <p>
     * 查询指定订单下的评论信息，通常用于订单详情页展示该订单的评论内容。
     * </p>
     *
     * @param orderId 订单ID，用于定位具体订单的评论
     * @param userId  当前登录用户ID，用于权限校验
     * @return 包含该订单评论信息的统一结果对象
     */
    Result getCommentsByOrderId(Long orderId, Long userId);

    /**
     * 添加商品评论
     * <p>
     * 用户对已购买的商品发表评论，包括评分、文字内容及可选的图片。
     * </p>
     *
     * @param comment 评论实体对象，包含评论内容、评分、商品ID等信息
     * @param userId  当前登录用户ID，即评论发布者
     * @return 包含添加结果的统一结果对象
     */
    Result addComment(GoodsComment comment, Long userId);

    /**
     * 追加评论
     * <p>
     * 用户对已发表的评论进行追加，补充更多的使用体验或反馈。
     * </p>
     *
     * @param append 追评实体对象，包含追评内容和关联的原评论ID
     * @param userId 当前登录用户ID，即追评发布者
     * @return 包含追评结果的统一结果对象
     */
    Result appendComment(CommentAppend append, Long userId);

    /**
     * 删除评论
     * <p>
     * 删除用户自己发表的评论，需要校验操作权限。
     * </p>
     *
     * @param commentId 评论ID，用于定位要删除的评论
     * @param userId    当前登录用户ID，用于校验是否为评论发布者本人
     * @return 包含删除结果的统一结果对象
     */
    Result deleteComment(Long commentId, Long userId);

    /**
     * 删除追评
     * <p>
     * 删除用户自己发表的追评内容，需要校验操作权限。
     * </p>
     *
     * @param appendId 追评ID，用于定位要删除的追评
     * @param userId   当前登录用户ID，用于校验是否为追评发布者本人
     * @return 包含删除结果的统一结果对象
     */
    Result deleteAppend(Long appendId, Long userId);

    /**
     * 删除评论回复
     * <p>
     * 删除评论下的回复内容，需要校验操作权限。
     * </p>
     *
     * @param reviewId 回复ID，用于定位要删除的回复
     * @param userId   当前登录用户ID，用于校验是否为回复发布者本人
     * @return 包含删除结果的统一结果对象
     */
    Result deleteReview(Long reviewId, Long userId);
}

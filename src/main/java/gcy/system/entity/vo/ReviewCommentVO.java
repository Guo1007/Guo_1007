package gcy.system.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审核评论视图对象，用于向前端展示审核评论及其嵌套子评论（回复）数据。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCommentVO {

    /**
     * 评论唯一标识
     */
    private Long id;

    /**
     * 关联的审核记录ID
     */
    private Long reviewId;

    /**
     * 发表评论的用户ID
     */
    private Long userId;

    /**
     * 发表评论的用户名称
     */
    @JsonProperty("userName")
    private String userName;

    /**
     * 发表评论的用户头像
     */
    @JsonProperty("userAvatar")
    private String userAvatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 被回复用户的ID
     */
    private Long replyToUserId;

    /**
     * 被回复用户的名称
     */
    @JsonProperty("replyToUserName")
    private String replyToUserName;

    /**
     * 被回复的父评论ID
     */
    private Long replyToCommentId;

    /**
     * 评论状态（如：正常、已删除等）
     */
    private Integer status;

    /**
     * 评论创建时间
     */
    private LocalDateTime createTime;

    /**
     * 软删除标记（0-未删除，1-已删除）
     */
    private int deleted;

    /**
     * 用户侧删除标记（0-未删除，1-已删除）
     */
    private int userDeleted;

    /**
     * 该评论的子回复列表
     */
    private List<ReviewCommentVO> children;
}

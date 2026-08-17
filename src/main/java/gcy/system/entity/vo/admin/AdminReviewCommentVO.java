package gcy.system.entity.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员审核评论视图对象，用于向后端管理界面展示审核流程中的评论数据，
 * 包含评论内容、评论人信息、回复关系及审核状态等字段。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewCommentVO {

    /** 评论唯一标识ID */
    private Long id;

    /** 关联的审核记录ID */
    private Long reviewId;

    /** 发表评论的用户ID */
    private Long userId;

    /** 发表评论的用户名称 */
    private String userName;

    /** 评论正文内容 */
    private String content;

    /** 被回复用户的ID，无回复时为空 */
    private Long replyToUserId;

    /** 被回复用户的名称，无回复时为空 */
    private String replyToUserName;

    /** 被回复评论的ID，无回复时为空 */
    private Long replyToCommentId;

    /** 评论状态标识（如正常、已删除等） */
    private Integer status;

    /** AI审核拒绝原因 */
    private String aiRejectReason;

    /** 人工审核拒绝原因 */
    private String manualRejectReason;

    /** 评论创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}

package gcy.system.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论追评视图对象，用于展示主评论的追加评价信息。
 * <p>
 * 包含追评内容、追评图片、追评时间，以及追评用户的昵称和头像等展示所需字段。
 * 对应数据库中评论追评相关的记录。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentAppendVO {

    /**
     * 追评记录主键ID
     */
    private Long id;

    /**
     * 关联的主评论ID
     */
    private Long mainCommentId;

    /**
     * 追评用户ID
     */
    private Long userId;

    /**
     * 删除标记（0-未删除，1-已删除）
     */
    private int deleted;

    /**
     * 用户端删除标记（0-未删除，1-已删除）
     */
    private int userDeleted;

    /**
     * 追评用户昵称
     */
    private String userName;

    /**
     * 追评用户头像URL
     */
    private String userAvatar;

    /**
     * 追评内容
     */
    private String appendContent;

    /**
     * 追评附带图片URL，多张图片以逗号分隔
     */
    private String appendImg;

    /**
     * 追评次数序号，表示该追评是第几次追加
     */
    private Integer appendNum;

    /**
     * 追评状态
     */
    private Integer status;

    /**
     * 追评时间
     */
    private LocalDateTime appendTime;
}

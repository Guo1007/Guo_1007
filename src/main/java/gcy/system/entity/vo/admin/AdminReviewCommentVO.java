package gcy.system.entity.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewCommentVO {

    private Long id;
    private Long reviewId;
    private Long userId;
    private String userName;
    private String content;
    private Long replyToUserId;
    private String replyToUserName;
    private Long replyToCommentId;
    private Integer status;
    private LocalDateTime createTime;
}

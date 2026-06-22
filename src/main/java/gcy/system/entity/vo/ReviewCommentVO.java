package gcy.system.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCommentVO {

    private Long id;

    private Long reviewId;

    private Long userId;

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("userAvatar")
    private String userAvatar;

    private String content;

    private Long replyToUserId;

    @JsonProperty("replyToUserName")
    private String replyToUserName;

    private Long replyToCommentId;

    private Integer status;

    private LocalDateTime createTime;

    private List<ReviewCommentVO> children;
}

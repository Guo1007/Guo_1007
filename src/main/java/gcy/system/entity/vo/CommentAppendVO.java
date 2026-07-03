package gcy.system.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentAppendVO {

    private Long id;

    private Long mainCommentId;

    private String userName;

    private String userAvatar;

    private String appendContent;

    private String appendImg;

    private Integer appendNum;

    private Integer status;

    private LocalDateTime appendTime;
}

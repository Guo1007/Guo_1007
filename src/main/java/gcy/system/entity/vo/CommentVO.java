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
public class CommentVO {

    private Long id;

    private Long orderId;

    private Long goodsId;

    private Long userId;

    private String userName;

    private String userAvatar;

    private Integer score;

    private String content;

    private String imgUrl;

    private String videoUrl;

    @JsonProperty("isAnonym")
    private Integer isAnonym;

    private Integer status;

    @JsonProperty("hasAppend")
    private Integer hasAppend;

    private List<CommentAppendVO> appendList;

    private LocalDateTime createTime;

    private int deleted;

    private int userDeleted;
}

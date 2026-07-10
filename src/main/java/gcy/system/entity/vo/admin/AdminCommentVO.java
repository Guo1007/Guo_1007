package gcy.system.entity.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommentVO {

    private Long id;

    private Long orderId;

    private Long goodsId;

    private String goodsName;

    private Long userId;

    private String userName;

    private Integer score;

    private String content;

    private String imgUrl;

    private String videoUrl;

    @JsonProperty("isAnonym")
    private Integer isAnonym;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

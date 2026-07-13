package gcy.system.entity.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAppendVO {

    private Long id;

    private Long mainCommentId;

    private Long userId;

    private String userName;

    private String goodsName;

    private String appendContent;

    private String appendImg;

    private Integer appendNum;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appendTime;
}

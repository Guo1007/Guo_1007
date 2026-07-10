package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("comment_append")
public class CommentAppend {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long mainCommentId;

    private Long userId;

    private String appendContent;

    private String appendImg;

    private Integer appendNum;

    private Integer status;

    private LocalDateTime appendTime;

    @TableLogic
    private Integer deleted = 0;

    /** 用户端软删除，与管理员 deleted 互不影响 */
    private Integer userDeleted = 0;
}

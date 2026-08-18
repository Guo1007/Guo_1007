package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 昵称审核记录实体，映射 nickname_review_log 表。
 *
 * @author 郭名城
 * @date 2026-08-18
 */
@Data
@TableName("nickname_review_log")
public class NicknameReviewLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String oldNickname;

    private String newNickname;

    private Integer status;

    private String aiRejectReason;

    private String manualRejectReason;

    private LocalDateTime createTime;
}
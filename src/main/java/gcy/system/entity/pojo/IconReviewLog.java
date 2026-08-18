package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 头像审核记录实体，映射 icon_review_log 表。
 *
 * @author 郭名城
 * @date 2026-08-18
 */
@Data
@TableName("icon_review_log")
public class IconReviewLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String oldIcon;

    private String newIcon;

    private Integer status;

    private String manualRejectReason;

    private LocalDateTime createTime;
}
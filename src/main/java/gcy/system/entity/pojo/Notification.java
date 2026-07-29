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
/**
 * 通知实体类，映射 notification 表。
 * 用于存储系统发送给用户的通知信息，包括标题、内容、通知类型及相关业务ID。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@TableName("notification")
public class Notification {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收通知的用户ID */
    private Long userId;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 通知类型 */
    private String type;

    /** 关联的审核ID */
    private Long reviewId;

    /** 关联的商品ID */
    private Long goodsId;

    /** 关联的审核评论ID */
    private Long reviewCommentId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 逻辑删除标记，0-未删除 */
    @TableLogic
    private Integer deleted = 0;
}

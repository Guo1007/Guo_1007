package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审核拒绝原因模板实体，映射 review_reject_reason 表。
 * 用于管理员预设常见的拒绝原因，审核时可快速选择。
 *
 * @author 郭名城
 * @date 2026-08-17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("review_reject_reason")
public class ReviewRejectReason {

    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 拒绝原因内容
     */
    private String reason;

    /**
     * 排序序号，越小越靠前
     */
    private Integer sortOrder;

    /**
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted = 0;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
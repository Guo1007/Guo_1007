package gcy.system.entity.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员端追加评论视图对象（VO），用于展示用户对商品评价的追评信息。
 * 包含追评人、商品名称、追评内容、追评图片、追评次数及追评时间等数据。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAppendVO {

    /**
     * 追评记录主键ID
     */
    private Long id;

    /**
     * 关联的主评论ID
     */
    private Long mainCommentId;

    /**
     * 追评用户ID
     */
    private Long userId;

    /**
     * 追评用户名称
     */
    private String userName;

    /**
     * 被评价的商品名称
     */
    private String goodsName;

    /**
     * 追评文本内容
     */
    private String appendContent;

    /**
     * 追评附带图片（JSON或逗号分隔的URL列表）
     */
    private String appendImg;

    /**
     * 追评序号（第几次追评）
     */
    private Integer appendNum;

    /**
     * 追评状态（如：0-正常，1-删除等）
     */
    private Integer status;

    /**
     * AI审核拒绝原因
     */
    private String aiRejectReason;

    /**
     * 人工审核拒绝原因
     */
    private String manualRejectReason;

    /**
     * 追评时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appendTime;
}

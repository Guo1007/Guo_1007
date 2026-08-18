package gcy.system.entity.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理后台评论视图对象，用于返回评论相关数据到管理端页面展示。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommentVO {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 评分
     */
    private Integer score;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论图片URL
     */
    private String imgUrl;

    /**
     * 评论视频URL
     */
    private String videoUrl;

    /**
     * 是否匿名（0-否 1-是）
     */
    @JsonProperty("isAnonym")
    private Integer isAnonym;

    /**
     * 评论状态（0-待审核 1-已通过 2-已拒绝）
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
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

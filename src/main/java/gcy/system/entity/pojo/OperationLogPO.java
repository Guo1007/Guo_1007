package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类，对应 operation_log 表。
 *
 * @author 郭名城
 * @date 2026-08-14
 */
@Data
@TableName("operation_log")
@Schema(description = "操作日志")
public class OperationLogPO {

    @TableId(type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "操作用户ID")
    private Long userId;

    @Schema(description = "操作用户名")
    private String userName;

    @Schema(description = "操作描述")
    private String operation;

    @Schema(description = "请求参数")
    private String params;

    @Schema(description = "耗时(毫秒)")
    private Integer duration;

    @Schema(description = "结果(成功/失败)")
    private String resultStatus;

    @Schema(description = "结果提示信息")
    private String resultMsg;

    @Schema(description = "客户端IP")
    private String ip;

    @Schema(description = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
package gcy.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员退款审核请求体。
 *
 * @author 郭名城
 * @date 2026-08-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundAuditDTO {

    /** 订单ID */
    @Schema(description = "订单ID")
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 审核是否通过：true=通过，false=不通过 */
    @Schema(description = "审核是否通过：true=通过，false=不通过")
    @NotNull(message = "审核结果不能为空")
    private Boolean passed;

    /** 审核备注（不通过时必填原因） */
    @Schema(description = "审核备注（不通过时必填原因）", maxLength = 500)
    @Size(max = 500, message = "备注不能超过500字")
    private String remark;
}

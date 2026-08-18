package gcy.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户申请退款请求体。
 *
 * @author 郭名城
 * @date 2026-08-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundApplyDTO {

    /**
     * 订单ID
     */
    @Schema(description = "订单ID")
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 退款原因
     */
    @Schema(description = "退款原因", maxLength = 500)
    @NotBlank(message = "退款原因不能为空")
    @Size(max = 500, message = "退款原因不能超过500字")
    private String refundReason;
}

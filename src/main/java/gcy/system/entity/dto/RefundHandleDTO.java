package gcy.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员退款处理请求体（拒绝退款/审核不通过）。
 *
 * @author 郭名城
 * @date 2026-08-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundHandleDTO {

    /**
     * 处理备注（拒绝/不通过原因）
     */
    @Schema(description = "处理备注（拒绝/不通过原因）", maxLength = 500)
    @NotBlank(message = "处理原因不能为空")
    @Size(max = 500, message = "备注不能超过500字")
    private String remark;
}

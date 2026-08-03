package gcy.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存预警项 DTO，用于携带家具名称及其当前库存数量，
 * 供前端展示库存预警信息。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockAlertItem {

    @Schema(description = "家具名称")
    private String fName;

    @Schema(description = "当前库存数量")
    private Integer stock;
}
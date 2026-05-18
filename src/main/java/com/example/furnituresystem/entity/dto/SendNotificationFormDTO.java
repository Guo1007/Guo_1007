package com.example.furnituresystem.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationFormDTO {

    /**
     * 目标用户ID，为空则发送给所有用户
     */
    private Long userId;

    @NotBlank(message = "通知标题不能为空")
    private String title;

    @NotBlank(message = "通知内容不能为空")
    private String content;

    /**
     * 类型: system/order/promotion
     */
    private String type = "system";
}

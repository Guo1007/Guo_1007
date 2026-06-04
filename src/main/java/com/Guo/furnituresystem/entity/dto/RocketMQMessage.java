package com.Guo.furnituresystem.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RocketMQMessage {

    private String type;

    private Long orderId;

    private Long userId;

    private String userEmail;

    private String userName;

    private String title;

    private String content;
}

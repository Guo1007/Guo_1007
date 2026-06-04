package com.Guo.furnituresystem.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationVO {

    private Long id;

    private Long userId;

    private String userName;

    private String title;

    private String content;

    private String type;

    private Boolean isRead;

    private LocalDateTime createTime;
}

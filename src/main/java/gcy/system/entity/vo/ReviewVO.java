package gcy.system.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewVO {

    private Long id;

    private Long userId;

    private String userName;

    private Long orderId;

    private Long furnitureId;

    private Integer rating;

    private String content;

    private LocalDateTime createTime;
}

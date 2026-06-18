package gcy.system.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {

    private String id;

    private Long userId;

    private BigDecimal totalPrice;

    private int status;

    private String consignee;

    private String phone;

    private String address;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private LocalDateTime payTime;

    private LocalDateTime shipTime;

    private List<OrderItemVO> itemList;

}
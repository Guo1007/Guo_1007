package gcy.system.entity.vo;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import gcy.system.entity.pojo.Order;
import gcy.system.entity.pojo.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    public static OrderVO from(Order order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        BeanUtil.copyProperties(order, vo);
        vo.setId(String.valueOf(order.getId()));
        vo.setItemList(items.stream().map(OrderItemVO::from).collect(Collectors.toList()));
        return vo;
    }
}
package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "`order`")
public class Order {

    private Long id;

    private Long userId;

    private BigDecimal totalPrice;

    private int status;

    private String consignee;

    private String phone;

    private String address;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

    private LocalDateTime shipTime;

    private LocalDateTime receiveTime;

    @TableField(exist = false)
    private List<OrderItem> itemList;

    /** 用户端软删除，与管理员 deleted 互不影响 */
    private Integer userDeleted = 0;

    @TableLogic
    private Integer deleted = 0;

}

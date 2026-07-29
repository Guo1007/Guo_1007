package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户收货地址实体，映射 user_address 表。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_address")
public class UserAddress {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 收货人姓名 */
    private String consignee;

    /** 收货人手机号 */
    private String phone;

    /** 收货详细地址 */
    private String address;

    /** 是否默认地址（1=是，0=否） */
    private Integer isDefault;

    /** 创建时间 */
    private LocalDateTime createTime;
}

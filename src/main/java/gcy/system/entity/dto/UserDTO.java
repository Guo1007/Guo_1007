package gcy.system.entity.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户数据传输对象（DTO），用于承载用户相关的请求和响应数据，
 * 包含用户基本信息、联系方式、收货地址等字段。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /** 用户唯一标识ID */
    private Long id;

    /** 用户名 */
    private String userName;

    /** 手机号码 */
    private String phone;

    /** 电子邮箱 */
    private String email;

    /** 用户密码（序列化时忽略） */
    @JsonIgnore
    private String passWord;

    /** 是否已设置密码 */
    private boolean hasPassword;

    /** 是否为管理员，0-否，1-是，默认为0 */
    private Integer isAdmin = 0;

    /** 用户头像图标路径 */
    private String icon;

    /** 收货地址 */
    private String address;

    /** 收货人姓名 */
    private String consignee;

    /** 收货人联系电话 */
    private String consigneePhone;


}

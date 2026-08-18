package gcy.system.entity.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

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
public class UserDTO implements java.io.Serializable {

    /**
     * 序列化版本号
     */
    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "用户唯一标识ID")
    private Long id;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "电子邮箱")
    private String email;

    @Schema(description = "用户密码（序列化时忽略）")
    @JsonIgnore
    private String passWord;

    @Schema(description = "是否已设置密码")
    private boolean hasPassword;

    @Schema(description = "是否为管理员，0-否，1-是，默认为0")
    private Integer isAdmin = 0;

    @Schema(description = "用户头像图标路径")
    private String icon;

    @Schema(description = "昵称审核状态：0=通过，1=待审核，2=已拒绝，3=待人工复审")
    private Integer nicknameReviewStatus;

    @Schema(description = "待审核的昵称")
    private String pendingNickname;

    @Schema(description = "头像审核状态：0=通过，1=待审核")
    private Integer iconReviewStatus;

    @Schema(description = "待审核的头像URL")
    private String pendingIcon;

    @Schema(description = "收货地址")
    private String address;

    @Schema(description = "收货人姓名")
    private String consignee;

    @Schema(description = "收货人联系电话")
    private String consigneePhone;


}

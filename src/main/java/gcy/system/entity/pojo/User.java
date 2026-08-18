package gcy.system.entity.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类，映射数据库 {@code user} 表，存储系统用户的基本信息，
 * 包括登录凭证、个人资料、收货地址及管理员标识等字段。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`user`")
public class User {

    /**
     * 用户主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 登录密码
     */
    private String passWord;

    /**
     * 用户头像URL，默认为空字符串
     */
    private String icon = "";

    /**
     * 昵称审核状态：0=通过/无待审，1=待AI审核，3=待人工复审，2=已拒绝
     */
    private Integer nicknameReviewStatus = 0;

    /**
     * 待审核的昵称
     */
    private String pendingNickname;

    /**
     * 头像审核状态：0=通过/无待审，1=待审核
     */
    private Integer iconReviewStatus = 0;

    /**
     * 待审核的头像URL
     */
    private String pendingIcon;

    /**
     * 收货地址
     */
    private String address;

    /**
     * 收货人姓名
     */
    private String consignee;

    /**
     * 收货人联系电话
     */
    private String consigneePhone;

    /**
     * 账户创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否为管理员，0-普通用户，1-管理员，默认为0
     */
    private Integer isAdmin = 0;

    /**
     * 逻辑删除标记，0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted = 0;

}

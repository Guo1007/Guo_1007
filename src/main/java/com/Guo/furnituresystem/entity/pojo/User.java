package com.Guo.furnituresystem.entity.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String email;

    private String userName;

    private String passWord;

    private String icon = "";

    private String address;

    private String consignee;

    private String consigneePhone;

    private LocalDateTime createTime;

    private Integer isAdmin = 0;

}

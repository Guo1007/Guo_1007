package com.Guo.furnituresystem.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("spec_value")
public class SpecValue {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long specGroupId;

    private String valueName;

    private String valueImage;

    private Integer sort;
}
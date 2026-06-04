package com.Guo.furnituresystem.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("furniture")
public class Furniture {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fName;

    private Long typeId;

    private BigDecimal price;

    private Integer stock;

    private String fIcon = "";

    private String intro;

    private String brand;

    private String images;

}

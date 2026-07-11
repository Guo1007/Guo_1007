package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("furniture")
public class Furniture {

    @TableId(type = IdType.AUTO)
    private Long id;

    @JsonProperty("fName")
    private String fName;

    private Long typeId;

    private BigDecimal price;

    private Integer stock;

    @JsonProperty("fIcon")
    private String fIcon = "";

    private String intro;

    private String brand;

    private String images;

    private String description;

    @TableLogic
    private Integer deleted = 0;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}

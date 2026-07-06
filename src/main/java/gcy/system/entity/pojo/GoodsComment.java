package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("goods_comment")
public class GoodsComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long orderItemId;

    private Long goodsId;

    private Long userId;

    private Integer score;

    private String content;

    private String imgUrl;

    private String videoUrl;

    @JsonProperty("isAnonym")
    private Integer isAnonym;

    private Integer status;

    @JsonProperty("hasAppend")
    private Integer hasAppend;

    private LocalDateTime latestAppendTime;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted = 0;
}

package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sku_spec")
public class SkuSpec {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long skuId;

    private Long specGroupId;

    private Long specValueId;
}
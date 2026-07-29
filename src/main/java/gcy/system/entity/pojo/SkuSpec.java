package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SKU规格关联实体，映射数据库 sku_spec 表。
 * 记录每个SKU所关联的规格分组及其规格值。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sku_spec")
public class SkuSpec {

    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * SKU ID，关联商品SKU
     */
    private Long skuId;

    /**
     * 规格分组ID，关联规格分组
     */
    private Long specGroupId;

    /**
     * 规格值ID，关联具体规格值
     */
    private Long specValueId;
}
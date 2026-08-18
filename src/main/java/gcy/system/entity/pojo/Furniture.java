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

/**
 * 家具实体类，映射数据库 furniture 表，记录家具的基本信息、库存、销售等数据。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("furniture")
public class Furniture {

    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 家具名称
     */
    @JsonProperty("fName")
    private String fName;

    /**
     * 家具类型ID，关联类型表
     */
    private Long typeId;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 家具图标路径
     */
    @JsonProperty("fIcon")
    private String fIcon = "";

    /**
     * 简介
     */
    private String intro;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 图片列表（JSON 格式存储多张图片路径）
     */
    private String images;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted = 0;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否推荐（0-否，1-是）
     */
    private Integer isRecommended;

    /**
     * 销量
     */
    private Integer saleCount;

}

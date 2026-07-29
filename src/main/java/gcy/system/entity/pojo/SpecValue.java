package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规格值实体类，映射 spec_value 表。
 * 表示某个规格分组下的具体规格值，如颜色分组下的"红色"、尺寸分组下的"XL"等。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("spec_value")
public class SpecValue {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属规格分组ID，关联 spec_group 表 */
    private Long specGroupId;

    /** 规格值名称，如"红色"、"XL" */
    private String valueName;

    /** 规格值配图URL */
    private String valueImage;

    /** 排序号，数值越小越靠前 */
    private Integer sort;
}
package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 规格分组实体，映射 spec_group 表。
 * 用于对家具的规格参数进行分组管理，例如将颜色、尺寸、材质等规格归类到不同的分组下。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("spec_group")
public class SpecGroup {

    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的家具ID
     */
    private Long furnitureId;

    /**
     * 规格分组名称，如"颜色"、"尺寸"、"材质"等
     */
    private String groupName;

    /**
     * 排序序号，用于控制分组在前端的显示顺序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
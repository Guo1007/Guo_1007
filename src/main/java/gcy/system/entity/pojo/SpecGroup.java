package gcy.system.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("spec_group")
public class SpecGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long furnitureId;

    private String groupName;

    private Integer sort;

    private LocalDateTime createTime;
}
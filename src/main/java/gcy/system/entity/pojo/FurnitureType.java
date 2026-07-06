package gcy.system.entity.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("furniture_type")
public class FurnitureType {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String title;

    private String icon = "";

    private String createTime;

    private String updateTime;

    @TableLogic
    private Integer deleted = 0;

}

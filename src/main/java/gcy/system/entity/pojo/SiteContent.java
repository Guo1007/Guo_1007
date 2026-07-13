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
@TableName("site_content")
public class SiteContent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sectionKey;

    private String sectionGroup;

    private String contentTitle;

    private String contentText;

    private String imageUrl;

    private String linkUrl;

    private String extraData;

    private Integer sortOrder;

    private Integer isActive;

    private LocalDateTime updatedAt;
}

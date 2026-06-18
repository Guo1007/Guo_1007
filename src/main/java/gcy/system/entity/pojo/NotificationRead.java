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
@TableName("notification_read")
public class NotificationRead {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long notificationId;

    private Long userId;

    private LocalDateTime readTime;
}

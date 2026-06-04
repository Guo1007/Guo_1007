package com.Guo.furnituresystem.entity.pojo;

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
@TableName("favorite")
public class Favorite {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long furnitureId;

    private LocalDateTime createTime;
}

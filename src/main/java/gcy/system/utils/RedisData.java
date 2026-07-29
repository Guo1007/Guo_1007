package gcy.system.utils;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * Redis缓存数据包装类，用于存储缓存数据及其过期时间。
 * 通过Lombok的@Data注解自动生成getter/setter/toString/equals/hashCode等方法。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
public class RedisData {

    /**
     * 缓存的数据对象
     */
    private Object data;

    /**
     * 缓存数据的过期时间
     */
    private LocalDateTime expireTime;

}

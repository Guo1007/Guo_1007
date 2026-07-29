package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户地址数据访问层映射接口
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供用户地址表（user_address）的基础 CRUD 操作，
 * 并扩展了清除默认地址的自定义 SQL 方法。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {

    /**
     * 清除指定用户的所有默认地址
     * <p>
     * 将指定用户的所有地址的 is_default 字段设置为 0（非默认），
     * 通常用于在设置新的默认地址之前，先清除旧的默认标记。
     * </p>
     *
     * @param userId 用户ID，用于定位需要清除默认地址的用户
     */
    @Update("UPDATE user_address SET is_default = 0 WHERE user_id = #{userId}")
    void clearDefault(@Param("userId") Long userId);

    /**
     * 清除指定用户除某条记录外的所有默认地址
     * <p>
     * 将指定用户下除 excludeId 所标识的地址之外的所有地址的 is_default 字段设置为 0（非默认），
     * 用于在将某条地址设为默认时，确保该用户只有该条地址保持默认状态。
     * </p>
     *
     * @param userId    用户ID，用于定位需要清除默认地址的用户
     * @param excludeId 排除的地址ID，该地址的默认状态不会被清除
     */
    @Update("UPDATE user_address SET is_default = 0 WHERE user_id = #{userId} AND id != #{excludeId}")
    void clearDefaultExcept(@Param("userId") Long userId, @Param("excludeId") Long excludeId);
}

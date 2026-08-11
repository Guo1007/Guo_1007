package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户数据访问层接口，继承 MyBatis-Plus 的 BaseMapper，
 * 提供对用户表（User）的基础 CRUD 操作（增删改查）。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 按手机号查询用户ID（含逻辑删除记录，绕开 @TableLogic 的 deleted=0 过滤）。
     * <p>
     * user 表 phone 存在唯一索引，逻辑删除的记录仍占用该值，
     * 新增/注册前需检查含已删除记录，避免插入时触发唯一索引冲突。
     * </p>
     *
     * @param phone 手机号
     * @return 命中记录的用户ID，不存在返回 null
     */
    @Select("SELECT id FROM `user` WHERE phone = #{phone} LIMIT 1")
    Long selectIdByPhone(String phone);

    /**
     * 按邮箱查询用户ID（含逻辑删除记录，绕开 @TableLogic 的 deleted=0 过滤）。
     * <p>
     * 与 {@link #selectIdByPhone(String)} 同理，用于唯一性检查。
     * </p>
     *
     * @param email 邮箱
     * @return 命中记录的用户ID，不存在返回 null
     */
    @Select("SELECT id FROM `user` WHERE email = #{email} LIMIT 1")
    Long selectIdByEmail(String email);

    /**
     * 逻辑删除用户并释放手机号/邮箱的唯一索引占用。
     * <p>
     * 逻辑删除记录仍保留在表中，phone/email 的唯一索引仍会拦截同名账号的新建，
     * 因此删除时同步将 phone、email 置空，使被删账号的号码/邮箱可以被复用。
     * </p>
     *
     * @param id 用户ID
     * @return 影响行数（0 表示用户不存在或已被删除）
     */
    @Update("UPDATE `user` SET deleted = 1, phone = NULL, email = NULL, update_time = NOW() " +
            "WHERE id = #{id} AND deleted = 0")
    int logicDeleteAndRelease(Long id);

}

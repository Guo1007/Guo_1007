package com.example.furnituresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.furnituresystem.entity.pojo.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {

    @Update("UPDATE user_address SET is_default = 0 WHERE user_id = #{userId}")
    void clearDefault(@Param("userId") Long userId);

    @Update("UPDATE user_address SET is_default = 0 WHERE user_id = #{userId} AND id != #{excludeId}")
    void clearDefaultExcept(@Param("userId") Long userId, @Param("excludeId") Long excludeId);
}

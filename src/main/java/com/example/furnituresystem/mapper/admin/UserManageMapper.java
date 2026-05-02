package com.example.furnituresystem.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.furnituresystem.entity.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserManageMapper extends BaseMapper<User> {
}

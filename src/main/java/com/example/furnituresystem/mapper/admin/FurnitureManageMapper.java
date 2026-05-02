package com.example.furnituresystem.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.furnituresystem.entity.pojo.Furniture;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FurnitureManageMapper extends BaseMapper<Furniture> {
}

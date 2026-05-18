package com.example.furnituresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.furnituresystem.entity.pojo.Furniture;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FurnitureMapper extends BaseMapper<Furniture> {

    @Select("select distinct brand from furniture")
    List<String> getFurnitureBrandsByTypeId(Long typeId);

    @Update("UPDATE furniture SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity}")
    int decrementStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Update("UPDATE furniture SET stock = stock + #{quantity} WHERE id = #{id}")
    int incrementStock(@Param("id") Long id, @Param("quantity") int quantity);
}

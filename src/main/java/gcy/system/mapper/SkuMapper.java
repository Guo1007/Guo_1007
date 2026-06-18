package gcy.system.mapper;

import gcy.system.entity.pojo.Sku;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface SkuMapper extends BaseMapper<Sku> {

    @Update("UPDATE sku SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity}")
    int decrementStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Update("UPDATE sku SET stock = stock + #{quantity} WHERE id = #{id}")
    int incrementStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Select("SELECT COALESCE(SUM(stock), 0) FROM sku WHERE furniture_id = #{furnitureId} AND status = 1")
    int sumStockByFurnitureId(@Param("furnitureId") Long furnitureId);

    @Select("SELECT COALESCE(MIN(price), 0) FROM sku WHERE furniture_id = #{furnitureId} AND status = 1 AND stock > 0")
    BigDecimal minPriceByFurnitureId(@Param("furnitureId") Long furnitureId);
}
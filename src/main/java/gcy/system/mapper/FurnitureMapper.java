package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.Furniture;
import gcy.system.entity.vo.LowStockVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FurnitureMapper extends BaseMapper<Furniture> {

    @Select("<script>select distinct brand from furniture where deleted = 0" +
            "<if test='typeId != null and typeId > 0'> and type_id = #{typeId}</if>" +
            "</script>")
    List<String> getFurnitureBrandsByTypeId(@Param("typeId") Long typeId);

    @Update("UPDATE furniture SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity}")
    int decrementStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Update("UPDATE furniture SET stock = stock + #{quantity} WHERE id = #{id}")
    int incrementStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Select("SELECT id, f_name, f_icon, stock FROM furniture WHERE stock < 10 AND deleted = 0 ORDER BY stock ASC")
    List<LowStockVO> selectLowStock();
}

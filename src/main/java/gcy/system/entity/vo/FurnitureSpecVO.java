package gcy.system.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品规格与SKU数据VO（前端展示用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FurnitureSpecVO {

    private List<SpecGroupVO> specGroups;

    private List<SkuVO> skuList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecGroupVO {
        private Long id;
        private String groupName;
        private Integer sort;
        private List<SpecValueVO> values;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecValueVO {
        private Long id;
        private String valueName;
        private String valueImage;
        private Integer sort;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkuVO {
        private Long id;
        private String skuCode;
        private BigDecimal price;
        private Integer stock;
        private String skuImage;
        private Integer status;

        private Map<String, String> specMap;

        private String specText;
    }
}
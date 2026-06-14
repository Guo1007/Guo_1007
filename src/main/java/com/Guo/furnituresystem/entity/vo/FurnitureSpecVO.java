package com.Guo.furnituresystem.entity.vo;

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

    /**
     * 规格组列表，每组包含其规格值
     */
    private List<SpecGroupVO> specGroups;

    /**
     * SKU列表，每个SKU包含规格关联信息
     */
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
        /**
         * 规格属性Map，如 {"颜色": "米白", "尺寸": "三人位"}
         */
        private Map<String, String> specMap;
        /**
         * 规格属性文本，如 "颜色:米白,尺寸:三人位"
         */
        private String specText;
    }
}
package gcy.system.entity.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端 - 商品规格与SKU组合保存DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FurnitureSpecDTO {

    private Long furnitureId;

    private List<SpecGroupDTO> specGroups;

    private List<SkuDTO> skuList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecGroupDTO {
        private Long id;           // 编辑时有值，新增时为null
        private String groupName;
        private Integer sort;
        private List<SpecValueDTO> values;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecValueDTO {
        private Long id;           // 编辑时有值，新增时为null
        private String valueName;
        private String valueImage;
        private Integer sort;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecPair {
        private String groupName;
        private String valueName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkuDTO {
        private Long id;           // 编辑时有值，新增时为null
        private String skuCode;
        private BigDecimal price;
        private Integer stock;
        private String skuImage;
        private Integer status;
        /**
         * 选中的规格值ID列表，用于生成sku_spec关联（优先使用specs进行名称匹配）
         */
        private List<Long> specValueIds;
        /**
         * 规格组合列表（推荐使用），通过规格组名称+值名称进行精确匹配，
         * 避免因规格值ID变动导致关联丢失
         */
        private List<SpecPair> specs;
    }
}
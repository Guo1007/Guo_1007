package gcy.system.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 用户收藏展示视图对象，用于前端展示用户收藏的家具信息，包含家具名称、图标、价格、库存、简介和品牌等字段。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteVO {

    /**
     * 收藏记录唯一标识
     */
    private Long id;

    /**
     * 家具名称
     */
    @JsonProperty("fName")
    private String fName;

    /**
     * 家具图标路径或URL
     */
    @JsonProperty("fIcon")
    private String fIcon;

    /**
     * 家具价格
     */
    private BigDecimal price;

    /**
     * 家具库存数量
     */
    private Integer stock;

    /**
     * 家具简介描述
     */
    private String intro;

    /**
     * 家具品牌
     */
    private String brand;
}

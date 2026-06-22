package gcy.system.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteVO {

    private Long id;

    @JsonProperty("fName")
    private String fName;

    @JsonProperty("fIcon")
    private String fIcon;

    private BigDecimal price;

    private Integer stock;

    private String intro;

    private String brand;
}

package gcy.system.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LowStockVO {

    private Long id;

    @JsonProperty("fName")
    private String fName;

    @JsonProperty("fIcon")
    private String fIcon;

    private Integer stock;
}

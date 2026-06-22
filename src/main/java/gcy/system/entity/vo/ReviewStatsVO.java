package gcy.system.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsVO {

    private BigDecimal avgRating;

    private Long reviewCount;
}

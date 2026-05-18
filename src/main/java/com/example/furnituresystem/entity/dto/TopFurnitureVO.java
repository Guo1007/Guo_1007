package com.example.furnituresystem.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopFurnitureVO {
    private Long furnitureId;
    private String furnitureName;
    private String furnitureIcon;
    private long totalSold;
}

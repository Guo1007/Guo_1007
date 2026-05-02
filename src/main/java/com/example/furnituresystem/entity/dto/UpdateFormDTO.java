package com.example.furnituresystem.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFormDTO {

    private String userName;

    private String address;

    private String consignee;

    private String consigneePhone;

    private String icon = "";

}

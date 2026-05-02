package com.example.furnituresystem.entity.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditUserFormDTO {

    private Long id;

    private String newPassword;

    private Integer isAdmin;
}
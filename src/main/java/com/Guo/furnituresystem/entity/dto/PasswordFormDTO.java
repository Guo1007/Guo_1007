package com.Guo.furnituresystem.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordFormDTO {

    private String oldPassword;

    private String newPassword;

    private String confirmPassword;

}

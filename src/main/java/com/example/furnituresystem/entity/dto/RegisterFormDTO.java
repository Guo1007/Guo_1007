package com.example.furnituresystem.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterFormDTO {

    private String phone;

    private String code;

    private String password;

    private String confirmPwd;

}

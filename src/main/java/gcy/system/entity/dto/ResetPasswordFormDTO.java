package gcy.system.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordFormDTO {

    private String email;

    private String code;

    private String newPassword;

    private String confirmPassword;

}

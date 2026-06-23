package gcy.system.entity.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;

    private String userName;

    private String phone;

    private String email;

    @JsonIgnore
    private String passWord;

    private boolean hasPassword;

    private Integer isAdmin = 0;

    private String icon;

    private String address;

    private String consignee;

    private String consigneePhone;


}

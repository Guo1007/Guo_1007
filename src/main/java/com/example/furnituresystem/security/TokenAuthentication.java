package com.example.furnituresystem.security;

import com.example.furnituresystem.entity.dto.UserDTO;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

@Getter
public class TokenAuthentication extends AbstractAuthenticationToken {

    private final UserDTO userDTO;

    private final String token;

    public TokenAuthentication(UserDTO userDTO, String token) {
        super(userDTO.getIsAdmin() != null && userDTO.getIsAdmin() == 1
                ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                : Collections.emptyList());
        this.userDTO = userDTO;
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return userDTO;
    }

}

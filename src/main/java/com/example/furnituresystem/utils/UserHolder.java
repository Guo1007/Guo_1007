package com.example.furnituresystem.utils;

import com.example.furnituresystem.entity.dto.UserDTO;

public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    private static final ThreadLocal<String> tokenTl = new ThreadLocal<>();

    public static void saveUser(UserDTO user, String token) {
        tl.set(user);
        tokenTl.set(token);
    }

    public static UserDTO getUser() {
        return tl.get();
    }

    public static String getToken() {
        return tokenTl.get();
    }

    public static void removeUser() {
        tl.remove();
        tokenTl.remove();
    }
}

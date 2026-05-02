package com.example.furnituresystem.utils;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RedisData {

    private Object data;

    private LocalDateTime expireTime;

}

package com.example.furnituresystem.utils;

public class RedisConstants {
    public static final String REGISTER_CODE_KEY = "register:code:";

    public static final Long REGISTER_CODE_TTL = 1L;

    public static final String LOGIN_CODE_KEY = "login:code:";

    public static final Long LOGIN_CODE_TTL = 1L;

    public static final String LOGIN_USER_KEY = "login:token:";

    public static final Long LOGIN_USER_TTL = 36000L;

    public static final Long CACHE_NULL_TTL = 2L;

    public static final Long CACHE_FURNITURE_TTL = 30L;

    public static final String CACHE_FURNITURE_KEY = "cache:furniture:";

    public static final String CACHE_FURNITURE_TYPE_KEY = "cache:furnitureTypeList:";

    public static final String LOCK_FURNITURE_KEY = "lock:furniture:";

    public static final String LOCK_FURNITURE_TYPE_KEY = "lock:furnitureTypeList";

    public static final Long LOCK_FURNITURE_TTL = 10L;

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";

    public static final String BLOG_LIKED_KEY = "blog:liked:";

    public static final String FEED_KEY = "feed:";

    public static final String SHOP_GEO_KEY = "shop:geo:";

    public static final String USER_SIGN_KEY = "sign:";

    public static final String LOGIN_USER_TOKEN_KEY = "login:user_token:";
}

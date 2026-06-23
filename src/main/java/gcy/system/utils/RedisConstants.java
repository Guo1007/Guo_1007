package gcy.system.utils;

public class RedisConstants {
    public static final String REGISTER_CODE_KEY = "register:code:";

    public static final Long REGISTER_CODE_TTL = 5L;

    public static final String LOGIN_CODE_KEY = "login:code:";

    public static final Long LOGIN_CODE_TTL = 5L;

    public static final String LOGIN_USER_KEY = "login:token:";

    public static final Long LOGIN_USER_TTL = 36000L;

    public static final Long CACHE_NULL_TTL = 2L;

    public static final Long CACHE_FURNITURE_TTL = 30L;

    public static final String CACHE_FURNITURE_KEY = "cache:furniture:";

    public static final String CACHE_FURNITURE_TYPE_KEY = "cache:furnitureTypeList:";

    public static final String LOCK_FURNITURE_KEY = "lock:furniture:";

    public static final String LOCK_FURNITURE_TYPE_KEY = "lock:furnitureTypeList";

    public static final String LOGIN_USER_TOKEN_KEY = "login:user_token:";

    public static final String ORDER_CREATE_KEY = "lock:order:create:";

    public static final String ORDER_PAY_KEY = "lock:order:pay:";

    public static final String ORDER_CANCEL_KEY = "lock:order:cancel:";

    public static final String ORDER_RECEIVE_KEY = "lock:order:confirm:";

    public static final String ORDER_SHIP_KEY = "lock:order:ship:";

}

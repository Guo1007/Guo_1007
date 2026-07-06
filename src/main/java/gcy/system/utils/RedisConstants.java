package gcy.system.utils;

/**
 * Redis Key 统一管理
 * 命名约定：前缀:业务:子业务 → 实际 key = 常量 + 业务ID
 */
public final class RedisConstants {

    private RedisConstants() {
    }

    // ==================== 验证码 ====================

    /** 注册验证码，后接邮箱/手机号 */
    public static final String REGISTER_CODE_KEY = "register:code:";
    public static final Long REGISTER_CODE_TTL = 5L;

    /** 登录验证码，后接邮箱/手机号 */
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 5L;

    /** 重置密码验证码，后接邮箱 */
    public static final String RESET_PASSWORD_CODE_KEY = "reset:code:";

    // ==================== 登录 Token ====================

    /** Redis Hash: 用户登录态，后接 token */
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 36000L;

    /** 用户最后一次使用的 token，后接 userId */
    public static final String LOGIN_USER_TOKEN_KEY = "login:user_token:";

    // ==================== 缓存 ====================

    /** 家具详情缓存，后接 furnitureId */
    public static final String CACHE_FURNITURE_KEY = "cache:furniture:";
    public static final Long CACHE_FURNITURE_TTL = 30L;

    /** 家具分类列表缓存 */
    public static final String CACHE_FURNITURE_TYPE_KEY = "cache:furnitureTypeList:";

    /** 缓存穿透空值 TTL（分钟） */
    public static final Long CACHE_NULL_TTL = 2L;

    // ==================== 分布式锁 ====================

    /** 家具缓存重建锁，后接 furnitureId */
    public static final String LOCK_FURNITURE_KEY = "lock:furniture:";

    /** 家具分类缓存重建锁 */
    public static final String LOCK_FURNITURE_TYPE_KEY = "lock:furnitureTypeList";

    /** 收藏操作锁，后接 userId:furnitureId（拼接方式：key + userId + ":" + furnitureId） */
    public static final String LOCK_FAVORITE_KEY = "lock:favorite:";

    /** 追评操作锁，后接 mainCommentId */
    public static final String LOCK_COMMENT_APPEND_KEY = "lock:comment:append:";

    // ==================== 订单锁 ====================

    /** 下单锁，后接 userId */
    public static final String ORDER_CREATE_KEY = "lock:order:create:";

    /** 支付锁，后接 orderId */
    public static final String ORDER_PAY_KEY = "lock:order:pay:";

    /** 取消订单锁，后接 orderId */
    public static final String ORDER_CANCEL_KEY = "lock:order:cancel:";

    /** 确认收货锁，后接 orderId */
    public static final String ORDER_RECEIVE_KEY = "lock:order:confirm:";

    /** 发货锁，后接 orderId */
    public static final String ORDER_SHIP_KEY = "lock:order:ship:";

    /** 超时取消定时任务锁（全局单实例执行） */
    public static final String ORDER_TIMEOUT_TASK_KEY = "lock:order:timeout:task";

    /** 库存预警定时任务锁（全局单实例执行） */
    public static final String STOCK_ALERT_TASK_KEY = "lock:stock:alert:task";

    // ==================== AI / 向量 ====================

    /** AI 对话记忆，后接 userId:conversationId */
    public static final String AI_CHAT_MEMORY_KEY = "ai:chat:";

    /** 知识库向量已摄入标记（幂等控制） */
    public static final String EMBEDDING_INGESTED_KEY = "ai:embedding:ingested";

    /** Redis 向量 key 通配符（用于批量 TTL 设置） */
    public static final String EMBEDDING_WILDCARD_KEY = "embedding:*";

}

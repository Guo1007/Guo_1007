package gcy.system.utils;

/**
 * Redis Key 统一管理常量类。
 * <p>
 * 命名约定：前缀:业务:子业务 → 实际 key = 常量 + 业务ID。
 * 本类集中管理验证码、登录 Token、业务缓存、分布式锁及 AI/向量等所有 Redis Key 前缀与 TTL 常量，
 * 避免项目中散落硬编码的 Key 字符串。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public final class RedisConstants {

    /**
     * 私有构造方法，防止外部实例化该工具常量类。
     */
    private RedisConstants() {
    }

    // ==================== 验证码 ====================

    /**
     * 注册验证码，后接邮箱/手机号
     */
    public static final String REGISTER_CODE_KEY = "register:code:";
    public static final Long REGISTER_CODE_TTL = 5L;

    /**
     * 登录验证码，后接邮箱/手机号
     */
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 5L;

    /**
     * 重置密码验证码，后接邮箱
     */
    public static final String RESET_PASSWORD_CODE_KEY = "reset:code:";

    // ==================== 登录 Token ====================

    /**
     * Redis Hash: 用户登录态，后接 token
     */
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 36000L;

    /**
     * 用户所有存活 Token 的 Set，后接 userId
     * 用途：管理员改密/删用户时批量定位该用户的全部 token
     */
    public static final String LOGIN_USER_TOKENS_SET = "login:user:tokens:set:";

    // ==================== 缓存 ====================

    /**
     * 家具详情缓存，后接 furnitureId
     */
    public static final String CACHE_FURNITURE_KEY = "cache:furniture:";
    public static final Long CACHE_FURNITURE_TTL = 30L;

    /**
     * 家具分类列表缓存
     */
    public static final String CACHE_FURNITURE_TYPE_KEY = "cache:furnitureTypeList:";

    /**
     * 缓存穿透空值 TTL（分钟）
     */
    public static final Long CACHE_NULL_TTL = 2L;

    // ==================== 分布式锁 ====================

    /**
     * 家具缓存重建锁，后接 furnitureId
     */
    public static final String LOCK_FURNITURE_KEY = "lock:furniture:";

    /**
     * 家具分类缓存重建锁
     */
    public static final String LOCK_FURNITURE_TYPE_KEY = "lock:furnitureTypeList";

    // ==================== 订单锁 ====================

    /**
     * 下单锁（防用户双击），后接 userId
     */
    public static final String ORDER_CREATE_KEY = "lock:order:create:";

    /**
     * 超时取消定时任务锁（全局单实例执行）
     */
    public static final String ORDER_TIMEOUT_TASK_KEY = "lock:order:timeout:task";

    /**
     * 库存预警定时任务锁（全局单实例执行）
     */
    public static final String STOCK_ALERT_TASK_KEY = "lock:stock:alert:task";

    // ==================== AI / 向量 ====================

    /**
     * AI 对话记忆，后接 userId:conversationId
     */
    public static final String AI_CHAT_MEMORY_KEY = "ai:chat:";

    /**
     * 知识库向量已摄入标记（幂等控制）
     */
    public static final String EMBEDDING_INGESTED_KEY = "ai:embedding:ingested";

    /**
     * Redis 向量 key 通配符（用于批量 TTL 设置）
     */
    public static final String EMBEDDING_WILDCARD_KEY = "embedding:*";

}

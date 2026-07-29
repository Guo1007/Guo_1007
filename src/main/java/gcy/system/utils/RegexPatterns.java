package gcy.system.utils;

/**
 * 正则表达式常量工具类，提供系统中常用的正则表达式模式。
 * <p>包含手机号、邮箱和密码的校验正则，均为静态常量，可直接引用使用。</p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public abstract class RegexPatterns {
    /**
     * 手机号正则表达式。
     * <p>用于校验中国大陆手机号码格式，支持主流运营商号段。</p>
     */
    public static final String PHONE_REGEX = "^1([38][0-9]|4[579]|5[0-35-9]|6[2567]|7[0-8]|9[0-9])\\d{8}$";
    /**
     * 邮箱正则表达式。
     * <p>用于校验电子邮箱地址格式，要求符合标准邮箱命名规则。</p>
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    /**
     * 密码正则表达式。
     * <p>6-32位，必须同时包含大写字母、小写字母和数字。</p>
     */
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,32}$";

}

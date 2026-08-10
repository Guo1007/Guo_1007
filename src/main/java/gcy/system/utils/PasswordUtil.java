package gcy.system.utils;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类，基于 BCrypt 算法提供密码加密和验证功能。
 * 使用 Spring Security 的 BCryptPasswordEncoder 进行安全的单向哈希加密。
 * @author 郭名城
 * @date 2026-07-30
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 对明文密码进行 BCrypt 加密，返回加密后的密文。
     *
     * @param rawPassword 原始明文密码
     * @return 加密后的密文字符串
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证明文密码是否与已加密的密文匹配。
     *
     * @param rawPassword     原始明文密码
     * @param encodedPassword 已加密的密文字符串
     * @return 如果明文密码与密文匹配则返回 true，否则返回 false
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

}

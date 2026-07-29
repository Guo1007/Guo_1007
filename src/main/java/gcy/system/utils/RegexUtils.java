package gcy.system.utils;

import cn.hutool.core.util.StrUtil;

/**
 * 正则表达式工具类，提供电话号码、邮箱、密码等格式验证功能。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public class RegexUtils {

    /**
     * 验证电话号码格式是否无效。
     *
     * @param phone 待验证的电话号码字符串
     * @return 如果号码为空或不匹配电话号码正则，返回 true；否则返回 false
     */
    public static boolean isPhoneInvalid(String phone) {
        return mismatch(phone, RegexPatterns.PHONE_REGEX);
    }

    /**
     * 验证邮箱格式是否有效。
     *
     * @param email 待验证的邮箱地址字符串
     * @return 如果邮箱非空且匹配邮箱正则，返回 true；否则返回 false
     */
    public static boolean isEmailValid(String email) {
        return !mismatch(email, RegexPatterns.EMAIL_REGEX);
    }

    /**
     * 验证密码格式是否有效。
     *
     * @param password 待验证的密码字符串
     * @return 如果密码非空且匹配密码正则，返回 true；否则返回 false
     */
    public static boolean isPasswordValid(String password) {
        return !mismatch(password, RegexPatterns.PASSWORD_REGEX);
    }

    /**
     * 检查字符串与给定正则表达式是否不匹配。若字符串为空则视为不匹配。
     *
     * @param str   待匹配的字符串
     * @param regex 正则表达式
     * @return 如果字符串为空或不匹配正则，返回 true；匹配正则则返回 false
     */
    private static boolean mismatch(String str, String regex) {
        if (StrUtil.isBlank(str)) {
            return true;
        }
        return !str.matches(regex);
    }

}

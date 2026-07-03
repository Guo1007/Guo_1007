package gcy.system.utils;

import cn.hutool.core.util.StrUtil;

public class RegexUtils {

    public static boolean isPhoneInvalid(String phone) {
        return mismatch(phone, RegexPatterns.PHONE_REGEX);
    }

    public static boolean isEmailValid(String email) {
        return !mismatch(email, RegexPatterns.EMAIL_REGEX);
    }

    public static boolean isPasswordValid(String password) {
        return !mismatch(password, RegexPatterns.PASSWORD_REGEX);
    }

    private static boolean mismatch(String str, String regex) {
        if (StrUtil.isBlank(str)) {
            return true;
        }
        return !str.matches(regex);
    }

}

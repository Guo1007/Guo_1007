package gcy.system.utils;

import gcy.system.entity.dto.UserDTO;

/**
 * 用户上下文持有者工具类。
 * 基于 ThreadLocal 实现，用于在同一线程内存储和获取当前登录用户信息及令牌，
 * 避免在方法调用链路中逐层传递用户参数。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    private static final ThreadLocal<String> tokenTl = new ThreadLocal<>();

    /**
     * 将当前用户信息与令牌保存到当前线程的 ThreadLocal 中。
     *
     * @param user  当前登录的用户数据传输对象
     * @param token 当前用户的认证令牌
     */
    public static void saveUser(UserDTO user, String token) {
        tl.set(user);
        tokenTl.set(token);
    }

    /**
     * 从当前线程的 ThreadLocal 中获取已保存的用户信息。
     *
     * @return 当前登录的用户数据传输对象，若未保存则返回 null
     */
    public static UserDTO getUser() {
        return tl.get();
    }

    /**
     * 从当前线程的 ThreadLocal 中获取已保存的认证令牌。
     *
     * @return 当前用户的认证令牌，若未保存则返回 null
     */
    public static String getToken() {
        return tokenTl.get();
    }

    /**
     * 清除当前线程 ThreadLocal 中保存的用户信息与令牌，防止内存泄漏。
     */
    public static void removeUser() {
        tl.remove();
        tokenTl.remove();
    }
}

package com.pethub.common.context;

/**
 * 基础上下文工具类。
 * 使用 ThreadLocal 保存当前请求对应的登录用户信息。
 */
public class BaseContext {

    private static final ThreadLocal<Long> CURRENT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USERNAME = new ThreadLocal<>();

    private BaseContext() {
    }

    public static void setCurrentId(Long id) {
        CURRENT_ID.set(id);
    }

    public static Long getCurrentId() {
        return CURRENT_ID.get();
    }

    public static void setCurrentUsername(String username) {
        CURRENT_USERNAME.set(username);
    }

    public static String getCurrentUsername() {
        return CURRENT_USERNAME.get();
    }

    /**
     * 请求结束后清理 ThreadLocal，避免线程复用时脏数据残留。
     */
    public static void clear() {
        CURRENT_ID.remove();
        CURRENT_USERNAME.remove();
    }
}

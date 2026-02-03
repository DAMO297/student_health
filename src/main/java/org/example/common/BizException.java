package org.example.common;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(String message) {
        this(500, message);
    }

    public static BizException unauthorized() {
        return new BizException(40101, "未登录或登录已过期");
    }

    public static BizException forbidden() {
        return new BizException(40301, "无权限");
    }

    public static BizException notFound(String message) {
        return new BizException(40401, message);
    }

    public static BizException conflict(String message) {
        return new BizException(40901, message);
    }
}

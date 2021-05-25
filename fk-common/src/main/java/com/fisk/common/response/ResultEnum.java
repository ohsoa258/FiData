package com.fisk.common.response;

/**
 * 请求返回结果类型
 * @author gy
 */

public enum ResultEnum {

    /**
     * 请求成功
     */
    SUCCESS(0, "成功"),

    /**
     * 请求成功
     */
    NOTFOUND(404,"未找到资源"),
    /**
     * 请求失败，系统报错
     */
    ERROR(500, "系统报错");

    ResultEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private final int code;
    private final String msg;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

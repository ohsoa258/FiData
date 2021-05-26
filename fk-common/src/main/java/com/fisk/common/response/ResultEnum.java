package com.fisk.common.response;

/**
 * 请求返回结果类型
 * @author gy
 */

public enum ResultEnum {

    /**
     * 返回码
     */
    SUCCESS(0, "成功"),
    NOTFOUND(404,"未找到资源"),
    ERROR(500, "系统报错"),

    /**
     * 平台级错误码，1000开始
     */
    SAVE_DATA_ERROR(1001, "数据保存失败"),
    SAVE_VERIFY_ERROR(1002, "数据验证失败"),
    DATA_NOTEXISTS(1003, "数据不存在")

    /**
     * 报表可视化服务，错误码从2000开始
     */;

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

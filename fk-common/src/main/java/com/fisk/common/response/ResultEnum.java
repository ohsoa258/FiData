package com.fisk.common.response;

/**
 * 请求返回结果类型
 *
 * @author gy
 */

public enum ResultEnum {

    /**
     * 返回码
     */
    SUCCESS(0, "成功"),
    UNAUTHORIZED(401, "未授权"),
    NOTFOUND(404, "未找到资源"),
    ERROR(500, "系统报错"),
    TIMEOUT(504, "请求超时"),
    UNKNOWN(999, "未知类型"),

    /**
     * 平台级错误码，1000开始
     */
    SAVE_DATA_ERROR(1001, "数据保存失败"),
    SAVE_VERIFY_ERROR(1002, "数据验证失败"),
    DATA_NOTEXISTS(1003, "数据不存在"),
    ENUM_TYPE_ERROR(1004, "错误的枚举类型"),
    UPDATE_DATA_ERROR(1005, "数据更新失败"),
    NAME_EXISTS(1006, "名称已存在"),
    PARAMTER_ERROR(1007, "请求参数有误"),
    DATA_EXISTS(1008, "数据已存在"),
    REMOTE_SERVICE_CALLFAILED(1009, "远程服务调用失败"),
    SERVER_FUSE(1010, "服务熔断"),
    PARAMTER_NOTNULL(1011, "参数不能为空"),

    /**
     * 报表可视化服务，错误码从2000开始
     */
    VISUAL_CONNECTION_ERROR(2001, "数据源连接失败"),
    VISUAL_LOADDRIVER_ERROR(2001, "数据库驱动加载失败"),
    VISUAL_PARAMTER_ERROR(2002, "缺少参数"),
    VISUAL_QUERY_ERROR(2003, "查询失败"),

    /**
     * 授权中心
     */
    AUTH_CLIENTINFO_ERROR(3001, ""),
    AUTH_SECRET_ERROR(3002, "客户端的信息有误,secret错误"),
    AUTH_JWT_ERROR(3003, "JWT无效或已过期"),
    AUTH_USER_NOTLOGIN(3004, "用户未登录"),
    AUTH_TOKEN_PARSER_ERROR(3005, "token解析失败"),
    AUTH_TOKEN_IS_NOTNULL(3006, "token不能为空"),

    /**
     * 用户中心
     */
    USER_ACCOUNTPASSWORD_ERROR(4001, "用户名或密码不正确"),

    /**
     * 数据接入模块
     */
    DATAACCESS_GETFIELD_ERROR(5001,"获取表字段失败"),

    /**
     * 后台任务模块
     */
    TASK_PUBLISH_ERROR(6001,"任务发布失败");

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

    public static ResultEnum getEnum(int code) {
        for (ResultEnum enums : ResultEnum.values()) {
            if (enums.getCode() == code) {
                return enums;
            }
        }
        return ResultEnum.UNKNOWN;
    }
}

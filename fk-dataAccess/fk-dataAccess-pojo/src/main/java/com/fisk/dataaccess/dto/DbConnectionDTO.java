package com.fisk.dataaccess.dto;

import lombok.Data;

/**
 * @author Lock
 */
@Data
public class DbConnectionDTO {
    /**
     * 驱动类型
     */
    public String driveType;

    /**
     * 主机名
     */
    public String host;

    /**
     * 端口号
     */
    public String port;

    /**
     * 数据库名
     */
    public String dbName;

    /**
     * 连接字符串
     */
    public String connectStr;

    /**
     * 连接账号
     */
    public String connectAccount;

    /**
     * 连接密码
     */
    public String connectPwd;
}

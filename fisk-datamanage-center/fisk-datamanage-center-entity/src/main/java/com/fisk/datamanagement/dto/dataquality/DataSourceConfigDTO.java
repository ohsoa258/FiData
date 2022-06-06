package com.fisk.datamanagement.dto.dataquality;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DataSourceConfigDTO {
    /**
     * 数据库类型
     */
    public String rdbmsType;
    /**
     * 主机名
     */
    public String hostName;
    /**
     * 端口
     */
    public String port;
    /**
     * 库名
     */
    public String dbName;
    /**
     * 用户名
     */
    public String userName;
    /**
     * 密码
     */
    public String password;

}

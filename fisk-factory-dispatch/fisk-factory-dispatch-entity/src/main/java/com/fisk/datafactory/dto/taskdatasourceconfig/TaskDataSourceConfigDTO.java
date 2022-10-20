package com.fisk.datafactory.dto.taskdatasourceconfig;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TaskDataSourceConfigDTO {
    /**
     * 主键
     */
    public long id;
    /**
     * 数据类型
     */
    public DataSourceTypeEnum type;
    /**
     * 服务器地址
     */
    public String host;
    /**
     * 端口
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
    /**
     * oracle服务类型：0服务名 1SID
     */
    public Integer serviceType;
    /**
     * oracle服务名
     */
    public String serviceName;
    /**
     * 组件id
     */
    public Integer taskId;

}

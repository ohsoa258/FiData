package com.fisk.datagovernance.vo.dataops;

import com.fisk.datagovernance.enums.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version 1.0
 * @description 数据运维日志VO
 * @date 2022/4/22 11:50
 */
@Data
public class DataOpsLogVO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * ip
     */
    @ApiModelProperty(value = "ip")
    public String conIp;

    /**
     * 数据库名称
     */
    @ApiModelProperty(value = "数据库名称")
    public String conDbname;

    /**
     * 数据库类型
     * 0、MYSQL
     * 1、SQLSERVER
     * 2、CUBE
     * 3、TABULAR
     * 4、POSTGRE
     */
    @ApiModelProperty(value = "数据库类型")
    public DataSourceTypeEnum conDbtype;

    /**
     * 执行的sql
     */
    @ApiModelProperty(value = "执行的sql")
    public String executeSql;

    /**
     * 执行结果 200:成功 500:失败
     */
    @ApiModelProperty(value = "执行结果 200:成功 500:失败")
    public int executeResult;

    /**
     * 执行消息
     */
    @ApiModelProperty(value = "执行消息")
    public String executeMsg;

    /**
     * 执行人
     */
    @ApiModelProperty(value = "执行人")
    public String executeUser;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    public String createUser;
}

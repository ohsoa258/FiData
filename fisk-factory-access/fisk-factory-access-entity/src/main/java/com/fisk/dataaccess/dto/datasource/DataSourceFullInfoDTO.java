package com.fisk.dataaccess.dto.datasource;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DataSourceFullInfoDTO {

    @ApiModelProperty(value = "数据库类型")
    public String conType;

    @ApiModelProperty(value = "数据源名称")
    public String sourceName;

    @ApiModelProperty(value = "连接类型")
    public String connector;

    @ApiModelProperty(value = "url地址")
    public String url;

    @ApiModelProperty(value = "ip")
    public String ip;

    @ApiModelProperty(value = "port")
    public String port;

    @ApiModelProperty(value = "dbName")
    public String dbName;

    @ApiModelProperty(value = "架构名")
    public String schemaName;

    @ApiModelProperty(value = "表名")
    public String tableName;

    @ApiModelProperty(value = "用户名")
    public String userName;

    @ApiModelProperty(value = "密码")
    public String password;

    @ApiModelProperty(value = "格式")
    public String format;

    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "sourceSql")
    public String sourceSql;

    @ApiModelProperty(value = "sinkSql")
    public String sinkSql;

    @ApiModelProperty(value = "insertSql")
    public String insertSql;

}

package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description api DTO
 * @date 2022/1/6 14:51
 */
@Data
public class ApiConfigDTO
{
    /**
     * api名称
     */
    @ApiModelProperty(value = "api名称")
    @NotNull()
    public String apiName;

    /**
     * api描述
     */
    @ApiModelProperty(value = "api描述")
    @NotNull()
    public String apiDesc;

    /**
     * 表名，带架构名
     */
    @ApiModelProperty(value = "表名，带架构名")
    public String tableName;

    /**
     * 表架构名
     */
    @ApiModelProperty(value = "表架构名")
    public String tableFramework;

    /**
     * 表名，不带架构名
     */
    @ApiModelProperty(value = "表名，不带架构名")
    public String tableRelName;

    /**
     * 表别名
     */
    @ApiModelProperty(value = "表别名")
    public String tableNameAlias;

    /**
     * 表类型 1：表  2：视图
     */
    @ApiModelProperty(value = "表类型 1：表  2：视图")
    public int tableType;

    /**
     * 表业务类型 1：事实表、2：维度表、3、指标表  4、宽表
     */
    @ApiModelProperty(value = "表业务类型 1：事实表、2：维度表、3、指标表  4、宽表")
    public int tableBusinessType;

    /**
     * 表路径
     */
    @ApiModelProperty(value = "表路径")
    public String tablePath;

    /**
     * sql语句
     */
    @ApiModelProperty(value = "sql语句")
    public String createSql;

    /**
     * sql语句，查询总条数
     */
    @ApiModelProperty(value = "sql语句，查询总条数")
    public String createCountSql;
    
    /**
     * api类型 1 sql、2 自定义sql
     */
    @ApiModelProperty(value = "api类型 1 sql、2 自定义sql")
    public int apiType;

    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int datasourceId;

    /**
     * 创建api类型
     */
    @ApiModelProperty(value = "创建api类型：1 创建新api 2 使用现有api")
    public Integer createApiType = 0;

    /**
     * 现有api地址
     */
    @ApiModelProperty(value = "现有api地址")
    public String apiAddress;

}

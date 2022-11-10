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
    @Length(min = 0, max = 50, message = "长度最多50")
    public String apiName;

    /**
     * api描述
     */
    @ApiModelProperty(value = "api描述")
    @NotNull()
    @Length(min = 0, max = 255, message = "长度最多255")
    public String apiDesc;

    /**
     * 表名
     */
    @ApiModelProperty(value = "表名")
    @Length(min = 0, max = 50, message = "长度最多255")
    public String tableName;

    /**
     * 表别名
     */
    @ApiModelProperty(value = "表别名")
    @Length(min = 0, max = 50, message = "长度最多255")
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
    @NotNull()
    @Length(min = 0, max = 5000, message = "长度最多5000")
    public String createSql;
    
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
}

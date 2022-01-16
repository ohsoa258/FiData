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
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String tableName;

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

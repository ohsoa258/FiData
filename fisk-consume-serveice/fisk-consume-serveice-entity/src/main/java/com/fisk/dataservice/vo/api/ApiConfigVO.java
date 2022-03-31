package com.fisk.dataservice.vo.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version v1.0
 * @description api VO
 * @date 2022/1/10 17:51
 */
@Data
public class ApiConfigVO {
    /**
     * Id
     */
    @ApiModelProperty(value = "主键")
    public int id;

    /**
     * api名称
     */
    @ApiModelProperty(value = "api名称")
    public String apiName;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int datasourceId;

    /**
     * api标识code
     */
    @ApiModelProperty(value = "api标识code")
    public String apiCode;

    /**
     * api描述
     */
    @ApiModelProperty(value = "api描述")
    public String apiDesc;

    /**
     * api类型 1 sql、2 自定义sql
     */
    @ApiModelProperty(value = "api类型 1 sql、2 自定义sql")
    public int apiType;

    /**
     * sql语句
     */
    @ApiModelProperty(value = "sql语句")
    public String createSql;

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

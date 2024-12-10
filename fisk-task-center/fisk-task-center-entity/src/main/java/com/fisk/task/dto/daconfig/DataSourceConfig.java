package com.fisk.task.dto.daconfig;

import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gy
 */
@Data
public class DataSourceConfig {
    @ApiModelProperty(value = "组件Id")
    public String componentId;
    @ApiModelProperty(value = "jdbcStr")
    public String jdbcStr;
    @ApiModelProperty(value = "类型")
    public DriverTypeEnum type;
    @ApiModelProperty(value = "用户")
    public String user;
    @ApiModelProperty(value = "密码")
    public String password;
    @ApiModelProperty(value = "同步模式")
    public int syncMode;
    @ApiModelProperty(value = "目标表名")
    public String targetTableName;
    /**
     * 物理表字段
     */
    @ApiModelProperty(value = "物理表字段")
    public List<TableFieldsDTO> tableFieldsList;

    /**
     * powerbi应用客户端id
     */
    @ApiModelProperty(value = "powerbi应用客户端id")
    public String powerbiClientId;

    /**
     * powerbi租户唯一标识符
     */
    @ApiModelProperty(value = "powerbi租户唯一标识符")
    public String powerbiClientSecret;

    /**
     * powerbi租户唯一标识符
     */
    @ApiModelProperty(value = "powerbi租户唯一标识符")
    public String powerbiTenantId;

    /**
     * powerbi数据集id
     */
    @ApiModelProperty(value = "powerbi数据集id")
    public String pbiDatasetId;

    /**
     * pbi查询语句
     */
    @ApiModelProperty(value = "pbi查询语句")
    public String pbiSql;

    /**
     * pbi 查询时所用的用户名
     */
    @ApiModelProperty(value = "pbi 查询时所用的用户名")
    public String pbiUsername;
}

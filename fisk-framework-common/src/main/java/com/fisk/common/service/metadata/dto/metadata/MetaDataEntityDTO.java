package com.fisk.common.service.metadata.dto.metadata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JinXingWang
 */
@Data
public class MetaDataEntityDTO extends MetaDataBaseAttributeDTO {

    @ApiModelProperty(value = "类型： 5 webapi 、13 视图 、14 数据库同步服务")
    public Integer entityType;

    @ApiModelProperty(value = "api类型：1 普通、2 自定义脚本 ")
    public Integer apiType;

    @ApiModelProperty(value = "表名,实体为api且api类型才会使用这个值")
    public String tableName;

    @ApiModelProperty(value = "1 创建新api、2 使用现有api、3 代理API")
    public int createApiType;

    @ApiModelProperty(value = "创建SQL")
    public String createSql;

    @ApiModelProperty(value = "数据源ID")
    public Integer datasourceDbId;

    @ApiModelProperty(value = "数据源类型 数据源类型：1Fidata(系统数据源,外部数据源) 2自定义(目前API模块存在此类型数据源，自定义数据源不存在于System模块DataSource表中)")
    public Integer datasourceType;

    @ApiModelProperty(value = "目标库Id")
    public Integer targetDbId;

    @ApiModelProperty(value = "目标表")
    public Integer targetTable;

    @ApiModelProperty(value = "应用程序名称")
    public String appName;

    @ApiModelProperty(value = "属性")
    public List<MetaDataColumnAttributeDTO>  attributeDTOList;
}

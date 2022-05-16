package com.fisk.datagovernance.dto.dataquality.datasource;

import com.fisk.datagovernance.enums.dataquality.TemplateTypeEnum;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version 1.0
 * @description 表字段查询DTO
 * @date 2022/4/7 15:49
 */
public class TableFieldQueryDTO {
    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int datasourceId;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    @NotNull(message = "表名称不可为null")
    public String tableName;

    /**
     * 表架构名称
     */
    @ApiModelProperty(value = "表架构名称，比如dbo")
    public String tableFramework;

    /**
     * 模板类型
     */
    @ApiModelProperty(value = "模板类型")
    public TemplateTypeEnum templateTypeEnum;
}

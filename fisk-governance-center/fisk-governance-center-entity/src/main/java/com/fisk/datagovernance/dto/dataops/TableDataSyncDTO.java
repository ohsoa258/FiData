package com.fisk.datagovernance.dto.dataops;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version 1.0
 * @description 表数据同步DTO
 * @date 2022/11/15 10:30
 */
@Data
public class TableDataSyncDTO {
    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int datasourceId;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    @NotNull()
    public String tableName;
}

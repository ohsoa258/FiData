package com.fisk.datagovernance.vo.dataops;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据运维表、字段
 * @date 2022/4/25 15:06
 */
@Data
public class DataOpsDataTableVO {
    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int datasourceId;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;
}

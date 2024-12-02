package com.fisk.datagovernance.dto.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;



@Data
public class DataSetPreviewDTO {

    /**
     * 数据集类型 1：行对比 2：值对比
     */
    @ApiModelProperty(value = "数据集类型 1：行对比 2：值对比")
    public int dataSetType;
    /**
     * apiId
     */
    @ApiModelProperty(value = "数据源id")
    public int dataSourceId;


    /**
     * sql
     */
    @ApiModelProperty(value = "sql")
    public String sql;
}

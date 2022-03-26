package com.fisk.datagovernance.dto.dataquality.businessfilter;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗组件排序DTO
 * @date 2022/3/25 23:30
 */
@Data
public class BusinessFilterSortDto {
    /**
     * id
     */
    @ApiModelProperty(value = "主键id")
    public int id;

    /**
     * 组件执行顺序
     */
    @ApiModelProperty(value = "主键id")
    public int moduleExecSort;
}

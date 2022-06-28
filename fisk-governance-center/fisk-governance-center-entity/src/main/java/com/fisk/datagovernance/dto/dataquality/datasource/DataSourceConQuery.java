package com.fisk.datagovernance.dto.dataquality.datasource;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version v1.0
 * @description 数据源 查询条件
 * @date 2022/3/22 14:51
 */
public class DataSourceConQuery {
    /**
     * 关键字
     */
    @ApiModelProperty(value = "关键字")
    public String keyword;

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码")
    public int current;

    /**
     * 页数
     */
    @ApiModelProperty(value = "页数")
    public int size;
}

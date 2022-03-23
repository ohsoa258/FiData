package com.fisk.common.filter.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FilterQueryDTO {

    /**
     * 列名
     */
    @ApiModelProperty(value = "列名", required = true)
    public String columnName;
    /**
     * 列名值
     */
    @ApiModelProperty(value = "输入内容", required = true)
    public String columnValue;
    /**
     * 查询类型--等于、大于、小于、包含
     */
    @ApiModelProperty(value = "查询类型--等于、大于、小于、包含", required = true)
    public String queryType;
}

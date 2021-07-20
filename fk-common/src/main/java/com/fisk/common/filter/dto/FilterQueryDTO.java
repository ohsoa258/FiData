package com.fisk.common.filter.dto;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FilterQueryDTO {

    /**
     * 列名
     */
    public String columnName;
    /**
     * 列名值
     */
    public String columnValue;
    /**
     * 查询类型--等于、大于、小于、包含
     */
    public String queryType;
}

package com.fisk.common.filter.dto;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FilterFieldDTO {
    /**
     * 列名
     */
    public String columnName;
    /**
     * 列名描述
     */
    public String columnDes;
    /**
     * 列名类型
     */
    public String columnType;
}

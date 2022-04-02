package com.fisk.common.core.utils.Dto;

/**
 * @author dick
 * @version v1.0
 * @description 参数信息
 * @date 2022/1/16 13:53
 */
public class SqlWhereDto {
    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 运算符
     * LIKE - 包含
     * EQU - 等于
     * NEQ - 不等于
     * LSS - 小于
     * LEQ - 小于或等于
     * GTR - 大于
     * GEQ - 大于或等于
     */
    public String operator;

    /**
     * 字段值
     */
    public String fieldValue;
}

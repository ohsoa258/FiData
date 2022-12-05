package com.fisk.dataservice.dto.tablefields;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableFieldDTO {

    /**
     * 表服务id
     */
    public Integer tableServiceId;

    /**
     * 源字段
     */
    public String sourceFieldName;

    /**
     * 源字段类型
     */
    public String sourceFieldType;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 显示名称
     */
    public String displayName;

    /**
     * 字段描述
     */
    public String fieldDes;

    /**
     * 字段类型
     */
    public String fieldType;

    /**
     * 字段长度
     */
    public Integer fieldLength;

    /**
     * 字段精度
     */
    public Integer fieldPrecision;

    /**
     * 是否主键
     */
    public Boolean primaryKey;

}

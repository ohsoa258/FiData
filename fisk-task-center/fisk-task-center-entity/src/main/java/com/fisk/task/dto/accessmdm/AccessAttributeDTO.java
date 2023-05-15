package com.fisk.task.dto.accessmdm;

import lombok.Data;

/**
 * @author wangjian
 */
@Data
public class AccessAttributeDTO {
    public long id;

    private Integer accessId;
    /**
     * 源表名称
     */
    private String sourceTableName;

    /**
     * 源字段名称
     */
    private String sourceFieldName;

    /**
     * 属性id
     */
    private Integer attributeId;

    /**
     * 业务主键
     */
    private Integer businessKey;
    /**
     * 字段名字
     */
    public String fieldName;
    /**
     * stg字段名字
     */
    public String mdmFieldName;
}

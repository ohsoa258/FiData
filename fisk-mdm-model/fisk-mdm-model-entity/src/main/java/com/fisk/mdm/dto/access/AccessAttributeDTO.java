package com.fisk.mdm.dto.access;

import lombok.Data;

/**
 * @author jianwenyang
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
}

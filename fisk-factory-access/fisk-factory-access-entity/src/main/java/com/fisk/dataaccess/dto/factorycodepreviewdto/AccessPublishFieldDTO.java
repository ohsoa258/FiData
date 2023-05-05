package com.fisk.dataaccess.dto.factorycodepreviewdto;

import lombok.Data;

/**
 * @author lishiji
 */
@Data
public class AccessPublishFieldDTO {

    /**
     * 维度表字段类型
     */
    public String fieldType;

    /**
     * 维度表字段长度
     */
    public int fieldLength;

    /**
     * 源字段名称
     */
    public String sourceFieldName;

    /**
     * 事实表：是否是业务覆盖标识 0:否 1:是
     */
    public int isBusinessKey;
}
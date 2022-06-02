package com.fisk.datamodel.dto.factattribute;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeDropDTO {
    public long id;
    public String factFieldEnName;
    public String factFieldType;
    /**
     * 0:退化维度，1:维度建，2:度量字段
     */
    public int attributeType;
}

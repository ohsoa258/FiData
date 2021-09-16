package com.fisk.dataaccess.dto;

import lombok.Data;

/**
 * @author Lock
 */
@Data
public class FieldNameDTO {
    public long id;
    public String fieldName;
    public String fieldType;
    public String fieldLength;
    public String fieldDes;
}

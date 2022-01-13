package com.fisk.datamodel.dto.tableconfig;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class SourceFieldDTO {
    public long id;

    public String fieldName;

    public String fieldDes;

    public String fieldType;

    public int fieldLength;

    public int primaryKey;
}

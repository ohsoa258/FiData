package com.fisk.dataservice.dto;

import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class DataDoFieldDTO {
    public Integer fieldId;
    public String fieldName;
    public String where;
    public String whereValue;
    public DataDoFieldTypeEnum fieldType;
    /**
     * 是否维度 0 否  1 是维度
     */
    public int dimension;
}

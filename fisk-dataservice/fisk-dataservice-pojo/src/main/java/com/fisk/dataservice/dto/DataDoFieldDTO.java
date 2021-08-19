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
    public DataDoFieldTypeEnum fieldType;
}

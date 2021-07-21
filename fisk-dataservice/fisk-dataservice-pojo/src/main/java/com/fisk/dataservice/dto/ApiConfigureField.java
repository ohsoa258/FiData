package com.fisk.dataservice.dto;

import com.fisk.dataservice.enums.ConfigureFieldTypeEnum;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/7/7 17:54
 */

@Data
public class ApiConfigureField {
    private Integer fieldId;
    private String field;
    private ConfigureFieldTypeEnum fieldType;
    private String fieldConditionValue;
    private String fieldValue;
}

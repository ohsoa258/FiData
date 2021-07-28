package com.fisk.dataservice.dto;

import com.fisk.dataservice.enums.ConfigureFieldTypeEnum;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/7/7 17:54
 */

@Data
public class ApiConfigureField {
    public Integer configureId;
    public String field;
    public ConfigureFieldTypeEnum fieldType;
    public String fieldConditionValue;
    public String fieldValue;
}

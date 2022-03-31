package com.fisk.datamodel.dto.widetableconfig;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class WideTableSourceFieldConfigDTO {

    public int fieldId;

    public String fieldName;

    public String fieldType;

    public int fieldLength;

    public String alias;
    /**
     * 可视化数据类型：文本、日期、数值
     */
    public String dataType;

}

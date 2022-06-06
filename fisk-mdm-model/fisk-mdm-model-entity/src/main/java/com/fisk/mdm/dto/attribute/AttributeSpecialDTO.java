package com.fisk.mdm.dto.attribute;

import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/5/26 17:34
 * @Version 1.0
 * 特殊字段  说明:影响后台表生成的字段
 */
@Data
public class AttributeSpecialDTO {

    private Integer id;
    /**
     * 名称
     */
    private String name;
    /**
     * 数据类型
     */
    private Integer dataType;
    /**
     * 数据类型长度
     */
    private Integer dataTypeLength;
    /**
     * 数据类型小数点长度
     */
    private Integer dataTypeDecimalLength;
    /**
     * 是否必填
     */
    private Boolean enableRequired;
}

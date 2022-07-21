package com.fisk.mdm.dto.attribute;

import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/7/21 11:13
 * @Version 1.0
 * 属性事实表
 */
@Data
public class AttributeFactDTO {

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
    private Integer enableRequired;
    /**
     * 属性id
     */
    private Integer attribute_id;
}

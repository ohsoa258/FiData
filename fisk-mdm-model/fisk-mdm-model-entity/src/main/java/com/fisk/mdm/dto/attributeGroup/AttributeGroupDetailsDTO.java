package com.fisk.mdm.dto.attributeGroup;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author WangYan
 * @Date 2022/5/23 8:39
 * @Version 1.0
 */
@Data
public class AttributeGroupDetailsDTO {

    private Integer id;
    @NotNull
    private Integer groupId;
    private Integer entityId;
    private Integer attributeId;
    /**
     * 属性名称
     */
    private String name;

    /**
     * 展示名称
     */
    private String displayName;

    /**
     * 描述
     */
    private String desc;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 数据类型长度
     */
    private Integer dataTypeLength;

    /**
     * 数据类型小数点长度
     */
    private Integer dataTypeDecimalLength;
}

package com.fisk.mdm.dto.attributeGroup;

import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/7/18 17:11
 * @Version 1.0
 */
@Data
public class AttributeQueryDTO {

    /**
     * 实体id
     */
    private Integer entityId;

    /**
     * 属性id
     */
    private Integer attributeId;
}

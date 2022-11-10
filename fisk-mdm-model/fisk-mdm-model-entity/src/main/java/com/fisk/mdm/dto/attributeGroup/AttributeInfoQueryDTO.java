package com.fisk.mdm.dto.attributeGroup;

import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/5/26 10:02
 * @Version 1.0
 */
@Data
public class AttributeInfoQueryDTO {

    /**
     * 属性组id
     */
    private Integer groupId;

    /**
     * 模型id
     */
    private Integer modelId;
}

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
}

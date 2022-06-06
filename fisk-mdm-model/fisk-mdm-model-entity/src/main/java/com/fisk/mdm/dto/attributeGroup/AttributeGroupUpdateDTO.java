package com.fisk.mdm.dto.attributeGroup;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author WangYan
 * @Date 2022/5/23 8:46
 * @Version 1.0
 */
@Data
public class AttributeGroupUpdateDTO {

    @NotNull
    private Integer id;
    private Integer modelId;
    private String name;
    private String details;
}

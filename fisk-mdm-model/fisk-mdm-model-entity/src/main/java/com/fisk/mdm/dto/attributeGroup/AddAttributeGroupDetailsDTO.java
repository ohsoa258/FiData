package com.fisk.mdm.dto.attributeGroup;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/23 8:39
 * @Version 1.0
 */
@Data
public class AddAttributeGroupDetailsDTO {

    @NotNull
    private Integer groupId;
    private Integer entityId;
    private List<Integer> attributeId;
}

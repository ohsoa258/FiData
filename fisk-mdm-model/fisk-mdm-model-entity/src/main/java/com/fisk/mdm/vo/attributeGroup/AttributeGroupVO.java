package com.fisk.mdm.vo.attributeGroup;

import com.fisk.mdm.dto.attributeGroup.AttributeGroupDetailsDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/23 8:38
 * @Version 1.0
 */
@Data
public class AttributeGroupVO {

    private Integer id;
    private String name;
    private String details;
    private List<AttributeGroupDetailsDTO> groupDetailsList;
}

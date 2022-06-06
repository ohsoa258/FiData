package com.fisk.mdm.vo.attributeGroup;

import com.fisk.mdm.dto.attributeGroup.AttributeGroupDetailsDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/25 14:40
 * @Version 1.0
 */
@Data
public class QueryAttributeGroupVO {

    /**
     * 实体id
     */
    private Integer id;
    /**
     * 实体名称
     */
    private String name;
    /**
     * 类型
     */
    private String type;

    /**
     * 属性信息
     */
    private List<AttributeGroupDetailsDTO> detailsDtoList;
}

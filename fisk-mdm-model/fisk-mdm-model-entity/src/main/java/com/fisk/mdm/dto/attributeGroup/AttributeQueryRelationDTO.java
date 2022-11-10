package com.fisk.mdm.dto.attributeGroup;

import com.fisk.mdm.vo.entity.EntityViewVO;
import lombok.Data;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/7/18 15:56
 * @Version 1.0
 */
@Data
public class AttributeQueryRelationDTO {

    private List<EntityViewVO> relationList;
    private List<AttributeInfoDTO> checkedArr;
}

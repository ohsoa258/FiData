package com.fisk.mdm.dto.attributeGroup;

import com.fisk.mdm.vo.entity.EntityViewVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/7/18 15:56
 * @Version 1.0
 */
@Data
public class AttributeQueryRelationDTO {

    @ApiModelProperty(value = "关系列表")
    private List<EntityViewVO> relationList;

    @ApiModelProperty(value = "核对数组")
    private List<AttributeInfoDTO> checkedArr;
}

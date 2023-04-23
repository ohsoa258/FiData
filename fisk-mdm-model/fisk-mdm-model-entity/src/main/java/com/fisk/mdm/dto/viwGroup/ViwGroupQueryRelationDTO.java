package com.fisk.mdm.dto.viwGroup;

import com.fisk.mdm.dto.entity.EntityQueryDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/31 10:32
 * @Version 1.0
 */
@Data
public class ViwGroupQueryRelationDTO {

    @ApiModelProperty(value = "关系列表")
    private List<EntityQueryDTO> relationList;
    @ApiModelProperty(value = "核对数组")
    private List<ViwGroupCheckDTO> checkedArr;
}

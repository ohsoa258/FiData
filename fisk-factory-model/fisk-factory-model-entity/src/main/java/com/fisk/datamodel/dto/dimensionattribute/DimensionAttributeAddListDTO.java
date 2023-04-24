package com.fisk.datamodel.dto.dimensionattribute;

import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
/**
 * @author cfk
 */
@Data
public class DimensionAttributeAddListDTO extends MQBaseDTO {

    @ApiModelProperty(value = "将维度属性添加到列表中")
    public List<DimensionAttributeAddDTO> dimensionAttributeAddDtoList;
}

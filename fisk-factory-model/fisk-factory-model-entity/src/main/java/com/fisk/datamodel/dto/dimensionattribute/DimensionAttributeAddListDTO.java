package com.fisk.datamodel.dto.dimensionattribute;

import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

import java.util.List;
/**
 * @author cfk
 */
@Data
public class DimensionAttributeAddListDTO extends MQBaseDTO {
    public List<DimensionAttributeAddDTO> dimensionAttributeAddDTOS;
}

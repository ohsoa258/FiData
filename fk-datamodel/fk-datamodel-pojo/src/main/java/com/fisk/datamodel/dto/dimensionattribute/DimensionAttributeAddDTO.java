package com.fisk.datamodel.dto.dimensionattribute;

import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDTO;
import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionAttributeAddDTO extends MQBaseDTO {
    public int dimensionId;
    public int createType;
    public List<DimensionAttributeDTO> list;
}

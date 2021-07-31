package com.fisk.datamodel.dto.dimension;

import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionAttributeAddDTO {
    public int dimensionId;
    public List<DimensionAttributeDTO> list;
}

package com.fisk.datamodel.dto;

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

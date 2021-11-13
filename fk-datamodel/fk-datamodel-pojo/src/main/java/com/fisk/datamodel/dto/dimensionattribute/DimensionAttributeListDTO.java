package com.fisk.datamodel.dto.dimensionattribute;

import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionAttributeListDTO {
    /**
     * sql脚本
     */
    public String sqlScript;

    public List<DimensionAttributeDTO> attributeDTOList;

}

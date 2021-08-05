package com.fisk.datamodel.dto.dimensionattribute;

import com.fisk.datamodel.dto.dimension.DimensionDTO;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DimensionAssociationDTO extends DimensionDTO {
    /**
     * 业务域名称
     */
    public String businessName;
}

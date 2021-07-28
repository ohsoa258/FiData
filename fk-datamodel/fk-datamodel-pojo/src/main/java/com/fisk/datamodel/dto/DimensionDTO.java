package com.fisk.datamodel.dto;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DimensionDTO {
    public int id;
    public int businessId;
    public String dimensionCnName;
    public String dimensionEnName;
    public String dimensionTabName;
    public String dimensionDesc;
}

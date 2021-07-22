package com.fisk.datamodel.dto;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ProjectDimensionDTO {
    public int id;
    public int businessId;
    public int dataId;
    public int projectId;
    public String dimensionCnName;
    public String dimensionEnName;
    public String dimensionTabName;
    public String dimensionDesc;
}

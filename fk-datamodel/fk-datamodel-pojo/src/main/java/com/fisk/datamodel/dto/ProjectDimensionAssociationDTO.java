package com.fisk.datamodel.dto;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ProjectDimensionAssociationDTO extends DimensionDTO {
    /**
     * 数据域名称
     */
    public String dataName;
    /**
     * 业务域名称
     */
    public String businessName;
}

package com.fisk.datamodel.dto.businessprocess;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessProcessAssociationDTO extends BusinessProcessDTO {
    /**
     * 业务域名称
     */
    public String businessName;
}

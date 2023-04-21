package com.fisk.datamodel.dto.businessprocess;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessProcessAssociationDTO extends BusinessProcessDTO {
    /**
     * 业务域名称
     */
    @ApiModelProperty(value = "业务域名称")
    public String businessName;
}

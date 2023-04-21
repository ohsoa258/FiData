package com.fisk.datamodel.dto.businessarea;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessAreaPublishDTO {
    /**
     * 业务域id
     */
    @ApiModelProperty(value = "业务域id")
    public int businessAreaId;
}

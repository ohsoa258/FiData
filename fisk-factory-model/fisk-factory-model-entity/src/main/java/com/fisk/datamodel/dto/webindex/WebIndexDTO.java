package com.fisk.datamodel.dto.webindex;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class WebIndexDTO {
    /**
     * 业务域数量
     */
    @ApiModelProperty(value = "业务域数量")
    public int businessAreaCount;

}

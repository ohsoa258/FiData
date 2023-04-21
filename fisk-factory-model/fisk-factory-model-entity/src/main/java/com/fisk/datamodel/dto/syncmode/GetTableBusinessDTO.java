package com.fisk.datamodel.dto.syncmode;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class GetTableBusinessDTO {

    @ApiModelProperty(value = "详细信息")
    public SyncTableBusinessDTO details;

}

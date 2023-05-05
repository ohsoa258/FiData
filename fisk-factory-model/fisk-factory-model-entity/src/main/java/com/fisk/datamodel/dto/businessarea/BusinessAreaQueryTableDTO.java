package com.fisk.datamodel.dto.businessarea;

import com.fisk.task.enums.OlapTableEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessAreaQueryTableDTO {

    @ApiModelProperty(value = "业务id")
    public Integer businessId;

    @ApiModelProperty(value = "表枚举")
    public OlapTableEnum tableEnum;

    @ApiModelProperty(value = "表id")
    public Integer tableId;

}

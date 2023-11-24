package com.fisk.mdm.dto.access;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class CustomScriptQueryDTO {

    @ApiModelProperty(value = "表id")
    public Integer tableId;

    /**
     * 执行类型:1stg 2 ods
     */
    @ApiModelProperty(value = "执行类型:1stg 2 ods")
    public Integer execType;

}

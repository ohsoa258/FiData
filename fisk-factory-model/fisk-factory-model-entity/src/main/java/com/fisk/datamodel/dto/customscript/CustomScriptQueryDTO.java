package com.fisk.datamodel.dto.customscript;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class CustomScriptQueryDTO {

    /**
     * 表类型:1维度 2事实
     */
    @ApiModelProperty(value = "表类型:1维度 2事实")
    public Integer type;

    @ApiModelProperty(value = "表Id")
    public Integer tableId;

    /**
     * 执行类型:1stg 2 ods
     */
    @ApiModelProperty(value = "执行类型:1stg 2 ods")
    public Integer execType;

}

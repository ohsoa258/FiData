package com.fisk.datamodel.dto.businessarea;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Lock
 * @version 2.6
 * @description 数据建模调转页面入参dto
 * @date 2022/6/15 15:05
 */
@Data
public class ModelRedirectDTO {

    @ApiModelProperty(value = "数仓维度、数仓事实、分析维度、分析事实、宽表", required = true)
    @NotNull
    private String tableType;

    @ApiModelProperty(value = "业务域主键id", required = true)
    @NotNull
    private Long businessId;

    @ApiModelProperty(value = "表主键id",required = true)
    @NotNull
    private Long tableId;
}

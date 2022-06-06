package com.fisk.datagovernance.dto.datasecurity.datamasking;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/4/1 13:53
 */
@Data
public class DataSourceIdDTO {

    @NotNull
    @ApiModelProperty(value = "数据源id", required = true)
    public String datasourceId;

    @NotNull
    @ApiModelProperty(value = "表id", required = true)
    public String tableId;
}

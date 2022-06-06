package com.fisk.chartvisual.dto.datasource;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author WangYan
 * @date 2022/3/19 20:55
 */
@Data
public class DataSourceIdDTO {

    @NotNull
    @ApiModelProperty(value = "数据源id")
    private Integer dataSourceId;
}

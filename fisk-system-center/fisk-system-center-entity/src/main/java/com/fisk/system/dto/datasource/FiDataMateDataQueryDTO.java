package com.fisk.system.dto.datasource;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version 1.0
 * @description FiData元数据查询DTO
 * @date 2022/6/13 11:58
 */
@Data
public class FiDataMateDataQueryDTO {
    @ApiModelProperty(value = "数据源id")
    @NotNull(message = "数据源id不可为null")
    public Integer datasourceId;

    @ApiModelProperty(value = "刷新数据源 true 刷新 false 不刷新")
    public boolean refreshDataSource;
}

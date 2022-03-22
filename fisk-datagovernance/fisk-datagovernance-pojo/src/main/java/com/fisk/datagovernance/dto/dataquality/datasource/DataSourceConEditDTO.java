package com.fisk.datagovernance.dto.dataquality.datasource;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 数据源编辑 DTO
 * @date 2022/1/6 14:51
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DataSourceConEditDTO extends DataSourceConDTO{

    @ApiModelProperty(value = "数据源id")
    @NotNull(message = "id不可为null")
    public Integer id;
}
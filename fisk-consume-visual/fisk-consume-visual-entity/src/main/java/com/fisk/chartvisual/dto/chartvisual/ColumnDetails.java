package com.fisk.chartvisual.dto.chartvisual;

import com.fisk.common.core.enums.chartvisual.AggregationTypeEnum;
import com.fisk.common.core.enums.chartvisual.ColumnTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author gy
 */
@Data
public class ColumnDetails {
    @ApiModelProperty(value = "专栏名称")
    @NotNull
    public String columnName;

    @ApiModelProperty(value = "专栏标签")
    public String columnLabel;
    @ApiModelProperty(value = "专栏类型")
    @NotNull
    public ColumnTypeEnum columnType;

    @ApiModelProperty(value = "集合类型")
    public AggregationTypeEnum aggregationType;
}

package com.fisk.chartvisual.dto.chartVisual;

import com.fisk.common.core.enums.chartvisual.AggregationTypeEnum;
import com.fisk.common.core.enums.chartvisual.ColumnTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author gy
 */
@Data
public class ColumnDetails {
    @NotNull
    public String columnName;
    public String columnLabel;
    @NotNull
    public ColumnTypeEnum columnType;

    public AggregationTypeEnum aggregationType;
}

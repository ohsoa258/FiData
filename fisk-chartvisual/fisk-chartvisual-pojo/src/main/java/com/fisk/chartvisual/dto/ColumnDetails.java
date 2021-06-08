package com.fisk.chartvisual.dto;

import com.fisk.common.enums.chartvisual.AggregationTypeEnum;
import com.fisk.common.enums.chartvisual.ColumnTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ColumnDetails {
    @NotNull
    public String columnName;
    public String columnLabel;
    @NotNull
    public ColumnTypeEnum columnType;

    public AggregationTypeEnum aggregationType;
}

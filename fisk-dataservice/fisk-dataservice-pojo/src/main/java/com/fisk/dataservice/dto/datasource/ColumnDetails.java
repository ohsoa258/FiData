package com.fisk.dataservice.dto.datasource;

import com.fisk.common.enums.dataservice.AggregationTypeEnum;
import com.fisk.common.enums.dataservice.ColumnTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description
 * @date 2022/1/6 14:51
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

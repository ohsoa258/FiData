package com.fisk.task.dto.task;

import lombok.Data;

@Data
public class TableFieldDetailDTO {

    public String tableSchema;
    public String tableName;
    public String ordinalPosition;
    public String columnName;
    public String dataType;
    public String characterMaximumLength;
    public String numericPrecision;
    public String numericScale;
    public String isNullable;
    public String columnDefault;
    public String description;
    public String udtName;


}

package com.fisk.chartvisual.dto;

import lombok.Data;

import java.util.List;

/**
 * @author gy
 */
@Data
public class ChartQueryFilter {
    public String columnName;
    public List<String> value;
}

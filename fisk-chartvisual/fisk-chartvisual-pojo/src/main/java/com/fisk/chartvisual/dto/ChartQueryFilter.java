package com.fisk.chartvisual.dto;

import com.fisk.chartvisual.enums.SsasChartFilterTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @author gy
 */
@Data
public class ChartQueryFilter {
    public String columnName;
    public List<String> value;
    public SsasChartFilterTypeEnum ssasChartFilterType;

    // 开始时间
    public String startTime;
    // 结束时间
    public String endTime;
    // 指定时间
    public String[] specifiedTime;
}

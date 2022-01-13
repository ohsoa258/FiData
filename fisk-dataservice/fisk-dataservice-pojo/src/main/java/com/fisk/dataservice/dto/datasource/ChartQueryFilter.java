package com.fisk.dataservice.dto.datasource;

import com.fisk.dataservice.enums.SsasChartFilterTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description
 * @date 2022/1/6 14:51
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

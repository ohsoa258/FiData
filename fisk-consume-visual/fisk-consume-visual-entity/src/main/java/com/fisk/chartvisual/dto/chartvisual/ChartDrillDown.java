package com.fisk.chartvisual.dto.chartvisual;

import lombok.Data;

/**
 * @author WangYan
 * @date 2022/1/6 15:58
 */
@Data
public class ChartDrillDown {
    public boolean isChartDrillDown;
    public int level;
    public String columnName;
}

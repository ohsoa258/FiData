package com.fisk.chartvisual.dto.chartVisual;

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

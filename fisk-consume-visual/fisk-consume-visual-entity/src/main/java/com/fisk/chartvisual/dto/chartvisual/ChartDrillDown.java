package com.fisk.chartvisual.dto.chartvisual;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/1/6 15:58
 */
@Data
public class ChartDrillDown {

    @ApiModelProperty(value = "isChartDrillDown")
    public boolean isChartDrillDown;

    @ApiModelProperty(value = "数量")
    public int level;

    @ApiModelProperty(value = "专栏名字")
    public String columnName;
}

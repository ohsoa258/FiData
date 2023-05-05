package com.fisk.chartvisual.dto.chartvisual;

import com.fisk.chartvisual.enums.SsasChartFilterTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gy
 */
@Data
public class ChartQueryFilter {
    @ApiModelProperty(value = "专栏名")
    public String columnName;

    @ApiModelProperty(value = "值")
    public List<String> value;

    @ApiModelProperty(value = "ssasChartFilterType")
    public SsasChartFilterTypeEnum ssasChartFilterType;

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    public String startTime;
    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    public String endTime;
    /**
     * 指定时间
     */
    @ApiModelProperty(value = "指定时间")
    public String[] specifiedTime;
}

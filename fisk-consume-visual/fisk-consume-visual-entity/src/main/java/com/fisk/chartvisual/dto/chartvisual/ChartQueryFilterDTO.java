package com.fisk.chartvisual.dto.chartvisual;

import com.fisk.chartvisual.enums.SsasChartFilterTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/1/6 15:58
 */
@Data
public class ChartQueryFilterDTO {
    /**
     * where字段名
     */
    @ApiModelProperty(value = "where字段名")
    public String columnName;
    /**
     * where条件
     */
    @ApiModelProperty(value = "where条件")
    private String where;
    /**
     * 字段的值
     */
    @ApiModelProperty(value = "字段的值")
    public List<String> value;

    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型")
    public SsasChartFilterTypeEnum ssasChartFilterType;

    /**
     * 切片器时间
     */
    @ApiModelProperty(value = "切片器时间")
    public String[] slicerTime;
}

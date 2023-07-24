package com.fisk.task.vo.statistics;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-07-20
 * @Description:
 */
@Data
public class LineChartVO {
    @ApiModelProperty("日期")
    public String date;
    @ApiModelProperty("成功数")
    public int failed;
    @ApiModelProperty("失败数")
    public int success;
}

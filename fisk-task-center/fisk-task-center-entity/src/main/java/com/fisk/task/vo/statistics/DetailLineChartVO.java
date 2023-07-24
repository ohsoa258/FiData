package com.fisk.task.vo.statistics;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-07-20
 * @Description:
 */
@Data
public class DetailLineChartVO {
    @ApiModelProperty("管道名称")
    public String workflowName;
    @ApiModelProperty("日期")
    public String date;
    @ApiModelProperty("运行时长")
    public String runningTime;
}

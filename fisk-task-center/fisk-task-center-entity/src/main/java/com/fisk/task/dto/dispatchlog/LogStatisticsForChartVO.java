package com.fisk.task.dto.dispatchlog;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author cfk
 */
@Data
public class LogStatisticsForChartVO {

    @ApiModelProperty(value = "成功日志")
    public List<LogStatisticsVO> successLog;
    @ApiModelProperty(value = "失败日志")
    public List<LogStatisticsVO> failureLog;
    @ApiModelProperty(value = "共计日志")
    public List<LogStatisticsVO> amountLog;

}

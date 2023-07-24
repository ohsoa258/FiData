package com.fisk.task.vo.statistics;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-07-20
 * @Description:
 */
@Data
public class TopRunningTimeVO {
    @ApiModelProperty("管道名称")
    public String workflowName;
    @ApiModelProperty("运行时间")
    public String runningTime;
}

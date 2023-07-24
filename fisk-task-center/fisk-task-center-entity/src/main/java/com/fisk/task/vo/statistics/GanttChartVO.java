package com.fisk.task.vo.statistics;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-07-18
 * @Description:
 */
@Data
public class GanttChartVO {
    @ApiModelProperty("管道名称")
    public String workflowName;
    @ApiModelProperty("横轴信息")
    public List<GanttChartTaskVO> ganttChartDetailVOList;
}

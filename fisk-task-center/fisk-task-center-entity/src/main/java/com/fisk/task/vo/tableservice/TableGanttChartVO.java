package com.fisk.task.vo.tableservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-07-18
 * @Description:
 */
@Data
public class TableGanttChartVO {
    @ApiModelProperty("表服务名称")
    public String tableServiceName;
    @ApiModelProperty("横轴信息")
    public List<TableGanttChartTaskVO> ganttChartDetailVOList;
}

package com.fisk.task.dto.pipeline;

import com.fisk.common.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @author cfk
 */
public class PipelineTableLogDTO extends BasePO {
    @ApiModelProperty(value = "组件id")
    public int componentId;
    @ApiModelProperty(value = "表id")
    public int tableId;
    @ApiModelProperty(value = "表类别")
    public int tableType;
    @ApiModelProperty(value = "状态")
    public int state;
    @ApiModelProperty(value = "备注")
    public String comment;
    @ApiModelProperty(value = "开始时间")
    public Date startTime;
    @ApiModelProperty(value = "结束时间")
    public Date endTime;
    @ApiModelProperty(value = "总条数")
    public int counts;
}

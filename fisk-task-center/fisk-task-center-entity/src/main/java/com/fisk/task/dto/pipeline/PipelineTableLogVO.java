package com.fisk.task.dto.pipeline;

import com.fisk.task.enums.OlapTableEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author cfk
 */
@Data
public class PipelineTableLogVO {
    @ApiModelProperty(value = "组件id")
    public int componentId;
    @ApiModelProperty(value = "表id")
    public Long tableId;
    @ApiModelProperty(value = "表类别")
    public OlapTableEnum tableType;
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
    @ApiModelProperty(value = "调用类型,0是手动调度,1是管道调度")
    public int dispatchType;
    @ApiModelProperty(value = "应用id")
    public Long appId;
    @ApiModelProperty(value = "表名称")
    public String tableName;
}

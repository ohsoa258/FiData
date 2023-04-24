package com.fisk.task.dto.nifi;

import com.fisk.task.dto.pipeline.NifiStageDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author cfk
 */
@Data
public class NifiStageMessageDTO {

    @ApiModelProperty(value = "主题")
    public String topic;
    @ApiModelProperty(value = "信息")
    public String message;
    @ApiModelProperty(value = "组id")
    public String groupId;
    @ApiModelProperty(value = "开始时间")
    public Date startTime;
    @ApiModelProperty(value = "结束时间")
    public Date endTime;
    @ApiModelProperty(value = "计数")
    public int counts;
    @ApiModelProperty(value = "nifi阶段DTO")
    public NifiStageDTO nifiStageDTO;
    /*
     * 管道批次号
     * */
    @ApiModelProperty(value = "管道批次号")
    public String pipelTraceId;

    /*
     * job批次号
     * */
    @ApiModelProperty(value = "job批次号")
    public String pipelJobTraceId;

    /*
     * task批次号
     * */
    @ApiModelProperty(value = "task批次号")
    public String pipelTaskTraceId;

    /*
     * stage批次号
     * */
    @ApiModelProperty(value = "stage批次号")
    public String pipelStageTraceId;

    /*
     * 流文件流入时间
     * */
    @ApiModelProperty(value = "流文件流入时间")
    public String entryDate;


}

package com.fisk.task.dto.task;

import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SftpCopyDTO extends MQBaseDTO {
    @ApiModelProperty(value = "管道批次id", required = true)
    public String pipelTraceId;
    /*
     * job批次号
     * */
    @ApiModelProperty(value = "管道批次id", required = true)
    public String pipelJobTraceId;

    /*
     * task批次号
     * */
    @ApiModelProperty(value = "管道批次id", required = true)
    public String pipelTaskTraceId;

    /*
     * stage批次号
     * */
    @ApiModelProperty(value = "管道批次id", required = true)
    public String pipelStageTraceId;

    @ApiModelProperty(value = "任务id", required = true)
    public String taskId;
}

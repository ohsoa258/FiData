package com.fisk.task.dto.kafka;

import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KafkaReceiveDTO extends MQBaseDTO {
    @ApiModelProperty(value = "表id")
    public Integer tableId;
    @ApiModelProperty(value = "表类型")
    public Integer tableType;
    @ApiModelProperty(value = "nifi自定义工作流详细Id")
    public Long nifiCustomWorkflowDetailId;
    @ApiModelProperty(value = "数字")
    public Integer numbers;
    @ApiModelProperty(value = "主题")
    public String topic;
    @ApiModelProperty(value = "结束时间")
    public Date endTime;
    @ApiModelProperty(value = "管道接口调度")
    public String pipelApiDispatch;

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
    * nifi定义的开始时间
    * */
    @ApiModelProperty(value = "nifi定义的开始时间")
    public String start_time;
    /*
    *nifi的总批次号
    * */
    @ApiModelProperty(value = "nifi的总批次号")
    public String fidata_batch_code;


    @ApiModelProperty(value = "如果任务开始")
    public boolean ifTaskStart;

    /*
    * topic的类别
    * */
    @ApiModelProperty(value = "topic的类别")
    public int topicType;

    /**
     * scriptTaskIds执行脚本任务id
     */
    @ApiModelProperty(value = "scriptTaskIds执行脚本任务id")
    public String scriptTaskIds;

    /**
     *sftpFileCopyTaskId,sftp复制任务
     */
    @ApiModelProperty(value = "sftpFileCopyTaskId,sftp复制任务")
    public String sftpFileCopyTaskIds;
    /**
     *powerbi数据集刷新任务
     */
    @ApiModelProperty(value = "powerbi数据集刷新任务")
    public String powerBiDataSetRefreshTaskIds;
    /**
     * 失败信息
     */
    @ApiModelProperty(value = "失败信息")
    public String message;


}

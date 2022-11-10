package com.fisk.task.dto.kafka;

import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

import java.util.Date;

@Data
public class KafkaReceiveDTO extends MQBaseDTO {
    public Integer tableId;
    public Integer tableType;
    public Long nifiCustomWorkflowDetailId;
    public Integer numbers;
    public String topic;
    public Date endTime;
    public String pipelApiDispatch;
    /*
    * 管道批次号
    * */
    public String pipelTraceId;

    /*
    * job批次号
    * */
    public String pipelJobTraceId;

    /*
    * task批次号
    * */
    public String pipelTaskTraceId;

    /*
    * stage批次号
    * */
    public String pipelStageTraceId;

    /*
    * nifi定义的开始时间
    * */
    public String start_time;
    /*
    *nifi的总批次号
    * */
    public String fidata_batch_code;


    public boolean ifTaskStart;

    /*
    * topic的类别
    * */
    public int topicType;


}

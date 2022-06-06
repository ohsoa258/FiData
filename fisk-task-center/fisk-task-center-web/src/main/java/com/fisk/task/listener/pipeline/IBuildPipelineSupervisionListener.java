package com.fisk.task.listener.pipeline;

import org.springframework.kafka.support.Acknowledgment;

import java.util.List;

/**
 * @author: cfk
 * CreateTime: 2022/04/21 15:05
 * Description:
 */
public interface IBuildPipelineSupervisionListener {

    void msg(List<String> dataInfo, Acknowledgment acke);
}

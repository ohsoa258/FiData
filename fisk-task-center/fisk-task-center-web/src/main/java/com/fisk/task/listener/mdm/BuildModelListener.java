package com.fisk.task.listener.mdm;

import org.springframework.kafka.support.Acknowledgment;

/**
 * @author WangYan
 * @date 2022/4/13 15:11
 */
public interface BuildModelListener {

    /**
     * 消费模型队列
     * @param dataInfo
     * @param acke
     */
    void msg(String dataInfo, Acknowledgment acke);
}

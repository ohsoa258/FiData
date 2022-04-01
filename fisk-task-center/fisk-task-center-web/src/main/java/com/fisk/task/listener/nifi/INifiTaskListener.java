package com.fisk.task.listener.nifi;

import org.springframework.kafka.support.Acknowledgment;

/**
 * @author: cfk
 * CreateTime: 2022/03/22 15:05
 * Description:
 */
public interface INifiTaskListener {
    void msg(String dataInfo, Acknowledgment acke);
}

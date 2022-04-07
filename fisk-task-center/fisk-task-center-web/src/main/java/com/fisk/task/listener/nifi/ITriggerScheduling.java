package com.fisk.task.listener.nifi;

import org.springframework.kafka.support.Acknowledgment;

/**
 * @author cfk
 */
public interface ITriggerScheduling {

    void unifiedControl(String data, Acknowledgment acknowledgment);
}

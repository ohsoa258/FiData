package com.fisk.task.listener.nifi;

import com.fisk.common.core.response.ResultEnum;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author cfk
 */
public interface ITriggerScheduling {

    ResultEnum unifiedControl(String data, Acknowledgment acknowledgment);
}

package com.fisk.task.listener.nifi;

import com.fisk.common.core.response.ResultEnum;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author cfk
 */
public interface IpowerBiListener {

    /**
     *
     * @param data
     * @param acke
     * @return
     */
    ResultEnum powerBiTask(String data, Acknowledgment acke);
}

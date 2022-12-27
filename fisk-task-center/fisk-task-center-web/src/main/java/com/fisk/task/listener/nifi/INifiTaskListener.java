package com.fisk.task.listener.nifi;

import com.fisk.common.core.response.ResultEnum;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author: cfk
 * CreateTime: 2022/03/22 15:05
 * Description:
 */
public interface INifiTaskListener {
    ResultEnum msg(String dataInfo, Acknowledgment acke);

    ResultEnum buildDataServices(String dataInfo, Acknowledgment acke);
}

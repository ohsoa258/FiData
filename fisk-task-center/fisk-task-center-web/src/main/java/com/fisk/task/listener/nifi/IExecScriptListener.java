package com.fisk.task.listener.nifi;

import com.fisk.common.core.response.ResultEnum;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author cfk
 */
public interface IExecScriptListener {

    public ResultEnum execScript(String data, Acknowledgment acke);
}

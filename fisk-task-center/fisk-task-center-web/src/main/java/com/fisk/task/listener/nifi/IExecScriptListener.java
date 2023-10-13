package com.fisk.task.listener.nifi;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.task.dto.task.ExecScriptDTO;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author cfk
 */
public interface IExecScriptListener {

    public ResultEnum execScript(String data, Acknowledgment acke);

    public ResultEnum execScriptToDispatch(String data,Acknowledgment acke);
}

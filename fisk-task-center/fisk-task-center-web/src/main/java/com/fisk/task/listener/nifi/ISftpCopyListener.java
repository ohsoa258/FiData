package com.fisk.task.listener.nifi;

import com.fisk.common.core.response.ResultEnum;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author cfk
 */
public interface ISftpCopyListener {

    ResultEnum sftpCopyTask(String data, Acknowledgment acke);
}

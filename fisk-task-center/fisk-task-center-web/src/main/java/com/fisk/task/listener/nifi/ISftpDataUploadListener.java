package com.fisk.task.listener.nifi;

import com.fisk.common.core.response.ResultEnum;

/**
 * @author cfk
 */
public interface ISftpDataUploadListener {
    /**
     * sftp或ftp-Java代码同步
     *
     * @param data
     * @return
     */
    ResultEnum buildSftpDataUploadListener(String data);
}

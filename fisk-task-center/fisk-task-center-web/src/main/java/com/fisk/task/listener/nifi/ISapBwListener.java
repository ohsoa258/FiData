package com.fisk.task.listener.nifi;

import com.fisk.common.core.response.ResultEnum;

/**
 * @author lsj
 */
public interface ISapBwListener {

    /**
     * sapbw-Java代码同步
     *
     * @param data
     * @return
     */
    ResultEnum sapBwToStg(String data);

}

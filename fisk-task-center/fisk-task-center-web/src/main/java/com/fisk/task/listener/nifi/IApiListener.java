package com.fisk.task.listener.nifi;

import com.fisk.common.core.response.ResultEntity;

/**
 * @author lsj
 */
public interface IApiListener {

    ResultEntity<Object> apiToStg(String data);

}

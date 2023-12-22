package com.fisk.task.listener.nifi;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.task.dto.DwLogQueryDTO;
import com.fisk.task.dto.DwLogResultDTO;

/**
 * @author lsj
 */
public interface IApiListener {

    ResultEntity<Object> apiToStg(String data);

    /**
     * dw数仓按时间获取单表nifi日志
     *
     * @param dwLogQueryDTO
     * @return
     */
    DwLogResultDTO getDwTblNifiLog(DwLogQueryDTO dwLogQueryDTO);

}

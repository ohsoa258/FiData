package com.fisk.dataservice.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.tablesyncmode.ApiTableSyncModeDTO;

/**
 * @author JianWenYang
 */
public interface ITableSyncMode {

    /**
     * 新增api服务调度
     *
     * @param dto
     * @return
     */
    ResultEnum addApiTableSyncMode(ApiTableSyncModeDTO dto);

}
